package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private String instructorName;
    private String thumbnailUrl;
    private BigDecimal price;
    private Integer durationHours;
    private String level;
    private Integer enrollmentCount;
}
