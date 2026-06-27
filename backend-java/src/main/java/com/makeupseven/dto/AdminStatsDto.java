package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminStatsDto {
    private long totalUsers;
    private long totalMuas;
    private long pendingVerification;
    private long totalBookings;
    private BigDecimal totalRevenue;
    private long activeBookings;
    private List<AdminMuaDto> pendingMuas;
}
