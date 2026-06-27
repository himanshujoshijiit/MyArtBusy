package com.makeupseven.service;

import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.model.enums.PaymentStatus;
import com.makeupseven.repository.BookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    @Lazy
    private final BookingService bookingService;
    @Lazy
    private final NotificationClient notificationClient;
    @Lazy
    private final ContractService contractService;

    @Value("${makeupseven.razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${makeupseven.razorpay.key-secret:}")
    private String razorpayKeySecret;

    public Map<String, Object> createDepositOrder(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (razorpayKeyId == null || razorpayKeyId.isBlank()) {
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
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("amount", booking.getDepositAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", "booking_" + bookingId);
            Order order = client.orders.create(options);
            booking.setRazorpayOrderId(order.get("id"));
            bookingRepository.save(booking);
            return Map.of(
                "orderId", order.get("id"),
                "amount", order.get("amount"),
                "currency", order.get("currency"),
                "keyId", razorpayKeyId,
                "mock", false
            );
        } catch (RazorpayException e) {
            throw new RuntimeException("Payment order creation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyPayment(String orderId, String paymentId, String signature) {
        Booking booking = bookingRepository.findAll().stream()
                .filter(b -> orderId.equals(b.getRazorpayOrderId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Booking not found for order"));

        if (razorpayKeyId != null && !razorpayKeyId.isBlank() && signature != null && !signature.equals("mock")) {
            try {
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", orderId);
                options.put("razorpay_payment_id", paymentId);
                options.put("razorpay_signature", signature);
                if (!Utils.verifyPaymentSignature(options, razorpayKeySecret)) {
                    throw new RuntimeException("Invalid payment signature");
                }
            } catch (Exception e) {
                throw new RuntimeException("Payment verification failed: " + e.getMessage());
            }
        }

        booking.setRazorpayPaymentId(paymentId);
        booking.setPaymentStatus(PaymentStatus.DEPOSIT_PAID);
        booking.setStatus(BookingStatus.DEPOSIT_PAID);
        booking.setContractUrl(contractService.contractUrlFor(booking));
        bookingRepository.save(booking);

        bookingService.markSlotBookedOnPayment(booking);
        notificationClient.sendBookingConfirmation(booking);
    }

    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        if (payload.containsKey("payload")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paymentEntity = (Map<String, Object>) payload.get("payload");
            if (paymentEntity != null && paymentEntity.containsKey("payment")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payment = (Map<String, Object>) ((Map<String, Object>) paymentEntity.get("payment")).get("entity");
                String orderId = (String) payment.get("order_id");
                String paymentId = (String) payment.get("id");
                verifyPayment(orderId, paymentId, null);
            }
        }
    }
}
