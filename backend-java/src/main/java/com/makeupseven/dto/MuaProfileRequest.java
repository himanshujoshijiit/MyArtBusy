package com.makeupseven.dto;

import com.makeupseven.model.enums.Occasion;
import com.makeupseven.model.enums.SkinTone;
import com.makeupseven.model.enums.SubscriptionTier;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class MuaProfileRequest {
    @NotBlank
    private String displayName;
    private String bio;
    @NotBlank
    private String city;
    private String locality;
    private String country;
    private String countryCode;
    private Set<Occasion> occasions;
    private Set<SkinTone> skinToneExpertise;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
