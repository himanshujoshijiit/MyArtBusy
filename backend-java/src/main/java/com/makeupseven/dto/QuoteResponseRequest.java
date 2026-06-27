package com.makeupseven.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteResponseRequest {
    private BigDecimal quotedAmount;
    private String muaResponse;
    private String status;
}
