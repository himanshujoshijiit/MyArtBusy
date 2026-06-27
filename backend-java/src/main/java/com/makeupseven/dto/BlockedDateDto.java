package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BlockedDateDto {
    private UUID id;
    private LocalDate blockDate;
    private String reason;
}
