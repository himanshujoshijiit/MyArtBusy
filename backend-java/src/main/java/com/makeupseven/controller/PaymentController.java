package com.makeupseven.controller;

import com.makeupseven.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/deposit/{bookingId}")
    public Map<String, Object> createDepositOrder(@PathVariable UUID bookingId) {
        return paymentService.createDepositOrder(bookingId);
    }

    @PostMapping("/verify")
    public Map<String, String> verifyPayment(@RequestBody Map<String, String> body) {
        paymentService.verifyPayment(body.get("orderId"), body.get("paymentId"), body.get("signature"));
        return Map.of("status", "success");
    }

    @PostMapping("/webhook")
    public Map<String, String> webhook(HttpServletRequest request,
                                       @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) throws Exception {
        String rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        paymentService.handleWebhook(rawBody, signature != null ? signature : "");
        return Map.of("status", "ok");
    }
}
