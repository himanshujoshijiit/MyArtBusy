package com.makeupseven.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtSecretValidator {

    private static final String[] INSECURE_MARKERS = {
            "dev-secret", "change-this", "change-in-production", "your-super-secret"
    };

    @Value("${makeupseven.jwt.secret}")
    private String jwtSecret;

    @Value("${makeupseven.require-secure-jwt:false}")
    private boolean requireSecureJwt;

    @PostConstruct
    void validate() {
        boolean insecure = jwtSecret.length() < 32;
        for (String marker : INSECURE_MARKERS) {
            if (jwtSecret.toLowerCase().contains(marker)) {
                insecure = true;
                break;
            }
        }
        if (insecure) {
            String msg = "JWT secret is weak or uses a default value. Set JWT_SECRET to a random 256-bit string.";
            if (requireSecureJwt) {
                throw new IllegalStateException(msg);
            }
            log.warn("SECURITY: {}", msg);
        }
    }
}
