package com.makeupseven.controller;

import com.makeupseven.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/pro/order")
    public Map<String, Object> createProOrder(Authentication auth) {
        return subscriptionService.createProOrder(UUID.fromString(auth.getName()));
    }

    @PostMapping("/pro/activate")
    public Map<String, String> activatePro(Authentication auth) {
        return subscriptionService.activatePro(UUID.fromString(auth.getName()));
    }
}
