package com.makeupseven.dto;

import com.makeupseven.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UUID userId;
    private String email;
    private String fullName;
    private UserRole role;
    private UUID muaProfileId;
}
