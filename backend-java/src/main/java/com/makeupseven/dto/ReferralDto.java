package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ReferralDto {
    private UUID id;
    private String referralCode;
    private BigDecimal creditAmount;
    private Boolean redeemed;
    private int totalReferrals;
}
