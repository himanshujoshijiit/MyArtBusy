package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EnrollResponse {
    private boolean enrolled;
    private CourseResponse course;
    private String enrollmentId;
    /** Present when Razorpay checkout is required before enrollment completes. */
    private Map<String, Object> payment;
}
