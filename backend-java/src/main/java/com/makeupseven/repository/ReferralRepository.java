package com.makeupseven.repository;

import com.makeupseven.model.Referral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReferralRepository extends JpaRepository<Referral, UUID> {
    Optional<Referral> findByReferralCode(String code);
    Optional<Referral> findByReferrerId(UUID referrerId);
}
