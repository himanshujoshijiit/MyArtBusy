package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionInfoDto {
    private String tier;
    private Integer bookingsUsed;
    private Integer bookingsLimit;
}
