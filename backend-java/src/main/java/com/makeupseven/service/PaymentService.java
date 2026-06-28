package com.makeupseven.service;

import com.makeupseven.config.RazorpayConfig;
import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.model.enums.PaymentStatus;
import com.makeupseven.repository.BookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BookingRepository bookingRepository;
    @Lazy
    private final BookingService bookingService;
    @Lazy
    private final NotificationClient notificationClient;
    @Lazy
    private final ContractService contractService;
    private final RazorpayConfig razorpayConfig;

    public Map<String, Object> createDepositOrder(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!razorpayConfig.isConfigured()) {
            log.info("Razorpay not configured — using mock payment for booking {}", bookingId);
            String mockOrderId = "order_mock_" + bookingId.toString().substring(0, 8);
            booking.setRazorpayOrderId(mockOrderId);
            bookingRepository.save(booking);
            return Map.of(
                "orderId", mockOrderId,
                "amount", booking.getDepositAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue(),
                "currency", "INR",
                "keyId", "rzp_test_mock",
                "mock", true
            );
        }

        try {
            RazorpayClient client = razorpayConfig.client();
            JSONObject options = new JSONObject();
            options.put("amount", booking.getDepositAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", RazorpayConfig.receipt("bk_", bookingId));
            Order order = client.orders.create(options);
            booking.setRazorpayOrderId(order.get("id"));
            bookingRepository.save(booking);
            return Map.of(
                "orderId", order.get("id"),
                "amount", order.get("amount"),
                "currency", order.get("currency"),
                "keyId", razorpayConfig.getKeyId(),
                "mock", false
            );
        } catch (RazorpayException e) {
            log.error("Razorpay order failed: {}", e.getMessage());
            throw new RuntimeException("Payment order creation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyPayment(String orderId, String paymentId, String signature) {
        Booking booking = bookingRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Booking not found for order"));

        if (booking.getPaymentStatus() == PaymentStatus.DEPOSIT_PAID) {
            log.info("Payment already verified for order {}", orderId);
            return;
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

        completeDepositPayment(booking, paymentId);
    }

    @Transactional
    public void handleWebhook(String rawBody, String signature) {
        if (razorpayConfig.isConfigured()) {
            if (razorpayConfig.getWebhookSecret() == null || razorpayConfig.getWebhookSecret().isBlank()) {
                throw new RuntimeException("Webhook secret not configured");
            }
            try {
                if (!Utils.verifyWebhookSignature(rawBody, signature, razorpayConfig.getWebhookSecret())) {
                    throw new RuntimeException("Invalid webhook signature");
                }
            } catch (Exception e) {
                throw new RuntimeException("Webhook verification failed: " + e.getMessage());
            }
        }

        JSONObject payload = new JSONObject(rawBody);
        if (!payload.has("payload")) return;

        JSONObject eventPayload = payload.getJSONObject("payload");
        if (!eventPayload.has("payment")) return;

        JSONObject payment = eventPayload.getJSONObject("payment").getJSONObject("entity");
        String orderId = payment.getString("order_id");
        String paymentId = payment.getString("id");

        Booking booking = bookingRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Booking not found for webhook order"));

        if (booking.getPaymentStatus() != PaymentStatus.DEPOSIT_PAID) {
            completeDepositPayment(booking, paymentId);
        }
    }

    private void completeDepositPayment(Booking booking, String paymentId) {
        booking.setRazorpayPaymentId(paymentId);
        booking.setPaymentStatus(PaymentStatus.DEPOSIT_PAID);
        booking.setStatus(BookingStatus.DEPOSIT_PAID);
        booking.setContractUrl(contractService.contractUrlFor(booking));
        bookingRepository.save(booking);
        bookingService.markSlotBookedOnPayment(booking);
        notificationClient.sendBookingConfirmation(booking);
    }
}
