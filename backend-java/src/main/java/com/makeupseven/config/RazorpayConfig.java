package com.makeupseven.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RazorpayConfig {

    @Value("${makeupseven.razorpay.key-id:}")
    private String keyId;

    @Value("${makeupseven.razorpay.key-secret:}")
    private String keySecret;

    @Value("${makeupseven.razorpay.webhook-secret:}")
    private String webhookSecret;

    /**
     * True only when Key ID and Secret look like real Razorpay credentials.
     * Placeholder values from .env.example (e.g. rzp_test_xxxxx) are treated as not configured.
     */
    public boolean isConfigured() {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            return false;
        }
        if (!keyId.matches("rzp_(test|live)_[A-Za-z0-9]{10,}")) {
            return false;
        }
        if (keySecret.length() < 24) {
            return false;
        }
        if (keyId.contains("xxxx") || keySecret.toLowerCase().contains("your_razorpay")) {
            return false;
        }
        return true;
    }

    public RazorpayClient client() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}
