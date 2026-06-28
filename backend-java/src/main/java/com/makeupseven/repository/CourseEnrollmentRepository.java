package com.makeupseven.repository;

import com.makeupseven.model.CourseEnrollment;
import com.makeupseven.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {

    boolean existsByUserIdAndCourseIdAndPaymentStatusIn(UUID userId, UUID courseId, List<PaymentStatus> statuses);

    Optional<CourseEnrollment> findByRazorpayOrderId(String razorpayOrderId);

    void deleteByUserIdAndCourseIdAndPaymentStatus(UUID userId, UUID courseId, PaymentStatus status);

    @Query("SELECT e.course.id FROM CourseEnrollment e WHERE e.user.id = :userId AND e.paymentStatus IN :statuses")
    List<UUID> findEnrolledCourseIds(@Param("userId") UUID userId, @Param("statuses") List<PaymentStatus> statuses);
}
