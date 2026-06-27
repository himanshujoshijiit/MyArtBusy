package com.makeupseven.dto;

import com.makeupseven.model.enums.BookingType;
import com.makeupseven.model.enums.Occasion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateBookingRequest {
    @NotNull
    private UUID muaId;
    private UUID serviceId;
    @NotNull
    private LocalDate bookingDate;
    @NotNull
    private LocalTime startTime;
    private Occasion occasion;
    private BookingType bookingType;
    private String notes;
}
