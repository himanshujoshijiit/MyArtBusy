package com.makeupseven.controller;

import com.makeupseven.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Map<String, String> webhook(@RequestBody Map<String, Object> payload) {
        paymentService.handleWebhook(payload);
        return Map.of("status", "ok");
    }
}
