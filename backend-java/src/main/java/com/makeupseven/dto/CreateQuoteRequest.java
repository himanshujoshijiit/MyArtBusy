package com.makeupseven.dto;

import com.makeupseven.model.enums.Occasion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateQuoteRequest {
    @NotNull
    private UUID muaId;
    private Occasion occasion;
    private LocalDate eventDate;
    private String details;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
}
