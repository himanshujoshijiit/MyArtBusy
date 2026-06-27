package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class AvailabilitySlotDto {
    private UUID id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean available;
}
