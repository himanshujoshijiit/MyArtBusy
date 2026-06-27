package com.makeupseven.dto;

import com.makeupseven.model.enums.Occasion;
import com.makeupseven.model.enums.SkinTone;
import com.makeupseven.model.enums.SubscriptionTier;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class MuaProfileResponse {
    private UUID id;
    private UUID userId;
    private String displayName;
    private String bio;
    private String city;
    private String locality;
    private String country;
    private String countryCode;
    private Set<Occasion> occasions;
    private Set<SkinTone> skinToneExpertise;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double rating;
    private Integer reviewCount;
    private Integer totalBookings;
    private Boolean topArtist;
    private Boolean verified;
    private Boolean featured;
    private Integer responseTimeMinutes;
    private String responseTimeLabel;
    private SubscriptionTier subscriptionTier;
    private List<PortfolioItemDto> portfolio;
    private List<MuaServiceDto> services;
}
