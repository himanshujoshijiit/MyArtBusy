package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID bookingId;
    private String clientName;
    private Integer rating;
    private String comment;
    private Boolean verified;
    private Instant createdAt;
}
