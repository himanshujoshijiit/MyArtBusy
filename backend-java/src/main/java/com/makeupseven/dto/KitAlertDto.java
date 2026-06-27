package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class KitAlertDto {
    private UUID id;
    private String name;
    private String alertType;
    private String message;
}
