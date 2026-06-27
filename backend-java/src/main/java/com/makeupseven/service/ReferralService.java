package com.makeupseven.service;

import com.makeupseven.dto.ReferralDto;
import com.makeupseven.model.Referral;
import com.makeupseven.model.User;
import com.makeupseven.repository.ReferralRepository;
import com.makeupseven.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;

    public ReferralDto getOrCreateReferralCode(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Referral referral = referralRepository.findByReferrerId(userId).orElseGet(() -> {
            String code = "MS" + userId.toString().substring(0, 6).toUpperCase();
            return referralRepository.save(Referral.builder()
                    .referrer(user)
                    .referralCode(code)
                    .creditAmount(BigDecimal.valueOf(200))
                    .build());
        });
        long total = referralRepository.findAll().stream()
                .filter(r -> r.getReferrer().getId().equals(userId) && r.getReferredUser() != null)
                .count();
        return ReferralDto.builder()
                .id(referral.getId())
                .referralCode(referral.getReferralCode())
                .creditAmount(referral.getCreditAmount())
                .redeemed(referral.getRedeemed())
                .totalReferrals((int) total)
                .build();
    }

    @Transactional
    public ReferralDto applyReferralCode(UUID newUserId, String code) {
        Referral referral = referralRepository.findByReferralCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid referral code"));
        if (referral.getReferredUser() != null) {
            throw new RuntimeException("Referral code already used");
        }
        User newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (referral.getReferrer().getId().equals(newUserId)) {
            throw new RuntimeException("Cannot use your own referral code");
        }
        referral.setReferredUser(newUser);
        referral.setRedeemed(true);
        referralRepository.save(referral);
        return getOrCreateReferralCode(newUserId);
    }
}
