package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminMuaDto {
    private String id;
    private String displayName;
    private String email;
    private String city;
    private boolean onboardingComplete;
}
