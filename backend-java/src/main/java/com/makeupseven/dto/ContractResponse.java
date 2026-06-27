package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class ContractResponse {
    private UUID bookingId;
    private String clientName;
    private String muaName;
    private String serviceName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal remainingAmount;
    private String terms;
    private String contractSignedAt;
    private Boolean signed;
}
