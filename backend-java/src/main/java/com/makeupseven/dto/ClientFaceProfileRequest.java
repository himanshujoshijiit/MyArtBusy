package com.makeupseven.dto;

import com.makeupseven.model.enums.SkinTone;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ClientFaceProfileRequest {
    private UUID clientId;
    private SkinTone skinTone;
    private String allergies;
    private String notes;
    private List<String> pastLooks;
}
