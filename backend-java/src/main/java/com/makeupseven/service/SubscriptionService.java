package com.makeupseven.service;

import com.makeupseven.config.RazorpayConfig;
import com.makeupseven.model.MuaProfile;
import com.makeupseven.model.enums.SubscriptionTier;
import com.makeupseven.repository.MuaProfileRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final MuaProfileRepository muaProfileRepository;
    private final RazorpayConfig razorpayConfig;

    @Value("${makeupseven.pro-tier-price:99900}")
    private int proTierPricePaise;

    public Map<String, Object> createProOrder(UUID userId) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));

        if (!razorpayConfig.isConfigured()) {
            return Map.of(
                "orderId", "order_pro_mock_" + mua.getId().toString().substring(0, 8),
                "amount", proTierPricePaise,
                "currency", "INR",
                "keyId", "rzp_test_mock",
                "mock", true,
                "muaId", mua.getId().toString()
            );
        }

        try {
            RazorpayClient client = razorpayConfig.client();
            JSONObject options = new JSONObject();
            options.put("amount", proTierPricePaise);
            options.put("currency", "INR");
            options.put("receipt", "pro_" + mua.getId());
            Order order = client.orders.create(options);
            return Map.of(
                "orderId", order.get("id"),
                "amount", order.get("amount"),
                "currency", order.get("currency"),
                "keyId", razorpayConfig.getKeyId(),
                "mock", false,
                "muaId", mua.getId().toString()
            );
        } catch (RazorpayException e) {
            throw new RuntimeException("Pro subscription order failed: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, String> activatePro(UUID userId) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        mua.setSubscriptionTier(SubscriptionTier.PRO);
        mua.setSubscriptionExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        muaProfileRepository.save(mua);
        return Map.of("status", "active", "tier", "PRO", "expiresAt", mua.getSubscriptionExpiresAt().toString());
    }
}
