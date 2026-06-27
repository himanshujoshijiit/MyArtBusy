package com.makeupseven.dto;

import com.makeupseven.model.enums.Occasion;
import com.makeupseven.model.enums.QuoteStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class QuoteRequestDto {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private UUID muaId;
    private String muaName;
    private Occasion occasion;
    private LocalDate eventDate;
    private String details;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private QuoteStatus status;
    private BigDecimal quotedAmount;
    private String muaResponse;
    private Instant createdAt;
}
