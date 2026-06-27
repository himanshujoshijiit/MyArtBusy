package com.makeupseven.dto;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String email;
    private String fullName;
    private String googleId;
}
