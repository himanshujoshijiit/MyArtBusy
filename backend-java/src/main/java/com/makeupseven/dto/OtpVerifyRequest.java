package com.makeupseven.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String phone;
    private String code;
    private String fullName;
}
