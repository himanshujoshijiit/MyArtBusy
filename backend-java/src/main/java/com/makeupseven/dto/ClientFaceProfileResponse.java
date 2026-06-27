package com.makeupseven.dto;

import com.makeupseven.model.enums.SkinTone;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ClientFaceProfileResponse {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private SkinTone skinTone;
    private String allergies;
    private String notes;
    private List<String> pastLooks;
    private Instant updatedAt;
}
