package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {
    private Integer totalBookings;
    private Integer monthlyBookings;
    private Integer pendingBookings;
    private BigDecimal totalEarnings;
    private BigDecimal monthlyEarnings;
    private BigDecimal netEarnings;
    private BigDecimal monthlyNetEarnings;
    private BigDecimal totalCommission;
    private Double averageRating;
    private Integer reviewCount;
    private SubscriptionInfoDto subscription;
    private List<KitAlertDto> kitAlerts;
}
