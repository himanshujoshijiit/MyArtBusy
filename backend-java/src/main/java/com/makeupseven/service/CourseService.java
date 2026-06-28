package com.makeupseven.service;

import com.makeupseven.config.RazorpayConfig;
import com.makeupseven.dto.CourseResponse;
import com.makeupseven.dto.EnrollResponse;
import com.makeupseven.model.Course;
import com.makeupseven.model.CourseEnrollment;
import com.makeupseven.model.User;
import com.makeupseven.model.enums.PaymentStatus;
import com.makeupseven.repository.CourseEnrollmentRepository;
import com.makeupseven.repository.CourseRepository;
import com.makeupseven.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private static final List<PaymentStatus> ACTIVE = List.of(PaymentStatus.FULLY_PAID, PaymentStatus.DEPOSIT_PAID);

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final RazorpayConfig razorpayConfig;

    public List<CourseResponse> listPublished() {
        return courseRepository.findByPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public CourseResponse getById(UUID id) {
        Course c = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
        return toResponse(c);
    }

    public List<UUID> getEnrolledCourseIds(UUID userId) {
        return enrollmentRepository.findEnrolledCourseIds(userId, ACTIVE);
    }

    @Transactional
    public EnrollResponse enroll(UUID userId, UUID courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!Boolean.TRUE.equals(course.getPublished())) {
            throw new RuntimeException("Course is not available");
        }

        if (enrollmentRepository.existsByUserIdAndCourseIdAndPaymentStatusIn(userId, courseId, ACTIVE)) {
            throw new RuntimeException("You are already enrolled in this course");
        }

        enrollmentRepository.deleteByUserIdAndCourseIdAndPaymentStatus(userId, courseId, PaymentStatus.PENDING);

        BigDecimal price = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;
        boolean freeOrMock = price.compareTo(BigDecimal.ZERO) <= 0 || !razorpayConfig.isConfigured();

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .user(user)
                .course(course)
                .amountPaid(price)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        enrollment = enrollmentRepository.save(enrollment);

        if (freeOrMock) {
            completeEnrollment(enrollment, "mock_" + System.currentTimeMillis());
            return EnrollResponse.builder()
                    .enrolled(true)
                    .course(toResponse(course))
                    .enrollmentId(enrollment.getId().toString())
                    .build();
        }

        try {
            RazorpayClient client = razorpayConfig.client();
            JSONObject options = new JSONObject();
            options.put("amount", price.multiply(BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", RazorpayConfig.receipt("cr_", enrollment.getId()));
            Order order = client.orders.create(options);
            String orderId = order.get("id").toString();
            enrollment.setRazorpayOrderId(orderId);
            enrollmentRepository.save(enrollment);

            return EnrollResponse.builder()
                    .enrolled(false)
                    .course(toResponse(course))
                    .enrollmentId(enrollment.getId().toString())
                    .payment(Map.of(
                            "orderId", orderId,
                            "amount", order.get("amount"),
                            "currency", order.get("currency"),
                            "keyId", razorpayConfig.getKeyId(),
                            "mock", false
                    ))
                    .build();
        } catch (RazorpayException e) {
            log.error("Course payment order failed: {}", e.getMessage());
            throw new RuntimeException("Could not start payment: " + e.getMessage());
        }
    }

    @Transactional
    public EnrollResponse verifyEnrollmentPayment(String orderId, String paymentId, String signature) {
        CourseEnrollment enrollment = enrollmentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found for this payment"));

        if (ACTIVE.contains(enrollment.getPaymentStatus())) {
            return EnrollResponse.builder()
                    .enrolled(true)
                    .course(toResponse(enrollment.getCourse()))
                    .enrollmentId(enrollment.getId().toString())
                    .build();
        }

        if (razorpayConfig.isConfigured() && signature != null && !signature.equals("mock")) {
            try {
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", orderId);
                options.put("razorpay_payment_id", paymentId);
                options.put("razorpay_signature", signature);
                if (!Utils.verifyPaymentSignature(options, razorpayConfig.getKeySecret())) {
                    throw new RuntimeException("Invalid payment signature");
                }
            } catch (Exception e) {
                throw new RuntimeException("Payment verification failed: " + e.getMessage());
            }
        }

        completeEnrollment(enrollment, paymentId);
        return EnrollResponse.builder()
                .enrolled(true)
                .course(toResponse(enrollment.getCourse()))
                .enrollmentId(enrollment.getId().toString())
                .build();
    }

    private void completeEnrollment(CourseEnrollment enrollment, String paymentId) {
        enrollment.setPaymentStatus(PaymentStatus.FULLY_PAID);
        enrollment.setRazorpayPaymentId(paymentId);
        enrollmentRepository.save(enrollment);

        Course course = enrollment.getCourse();
        course.setEnrollmentCount(course.getEnrollmentCount() + 1);
        courseRepository.save(course);
        log.info("User {} enrolled in course {}", enrollment.getUser().getId(), course.getId());
    }

    private CourseResponse toResponse(Course c) {
        return CourseResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .instructorName(c.getInstructorName())
                .thumbnailUrl(c.getThumbnailUrl())
                .price(c.getPrice())
                .durationHours(c.getDurationHours())
                .level(c.getLevel())
                .enrollmentCount(c.getEnrollmentCount())
                .build();
    }
}
