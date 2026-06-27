package com.makeupseven.dto;

import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.model.enums.BookingType;
import com.makeupseven.model.enums.Occasion;
import com.makeupseven.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private UUID muaId;
    private String muaName;
    private UUID serviceId;
    private String serviceName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Occasion occasion;
    private BookingType bookingType;
    private String notes;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal commissionAmount;
    private BigDecimal remainingAmount;
    private BigDecimal refundAmount;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String razorpayOrderId;
    private String contractUrl;
    private String contractSignedAt;
    private Boolean contractSigned;
    private Instant createdAt;
}
