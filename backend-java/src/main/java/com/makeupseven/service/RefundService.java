package com.makeupseven.service;

import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.PaymentStatus;
import com.makeupseven.repository.BookingRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final BookingRepository bookingRepository;

    @Value("${makeupseven.razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${makeupseven.razorpay.key-secret:}")
    private String razorpayKeySecret;

    /** 48-hour cancellation policy: full refund if cancelled 48+ hrs before event. */
    @Transactional
    public BigDecimal processCancellationRefund(Booking booking) {
        if (booking.getPaymentStatus() != PaymentStatus.DEPOSIT_PAID) {
            return BigDecimal.ZERO;
        }

        long hoursUntil = ChronoUnit.HOURS.between(
                java.time.LocalDateTime.now(),
                booking.getBookingDate().atTime(booking.getStartTime()));

        BigDecimal deposit = booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount;

        if (hoursUntil >= 48) {
            refundAmount = deposit;
        } else {
            refundAmount = BigDecimal.ZERO;
            log.info("Deposit forfeited for booking {} — cancelled within 48hrs", booking.getId());
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0 && booking.getRazorpayPaymentId() != null) {
            processRazorpayRefund(booking, refundAmount);
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        booking.setRefundAmount(refundAmount);
        bookingRepository.save(booking);
        return refundAmount;
    }

    private void processRazorpayRefund(Booking booking, BigDecimal amount) {
        if (razorpayKeyId == null || razorpayKeyId.isBlank()) {
            log.info("Mock refund ₹{} for booking {}", amount, booking.getId());
            return;
        }
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            Refund refund = client.payments.refund(booking.getRazorpayPaymentId(), options);
            log.info("Razorpay refund {} for booking {}", refund.get("id"), booking.getId());
        } catch (RazorpayException e) {
            log.error("Refund failed for booking {}: {}", booking.getId(), e.getMessage());
            throw new RuntimeException("Refund processing failed: " + e.getMessage());
        }
    }
}
