package com.makeupseven.service;

import com.makeupseven.dto.*;
import com.makeupseven.model.*;
import com.makeupseven.repository.MuaProfileRepository;
import com.makeupseven.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MuaProfileService {

    private final MuaProfileRepository muaProfileRepository;
    private final UserRepository userRepository;

    public MuaProfileResponse getById(UUID id) {
        return toResponse(muaProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MUA not found")));
    }

    public List<MuaProfileResponse> getAll() {
        return muaProfileRepository.findByActiveTrue().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MuaProfileResponse> getByCity(String city) {
        return muaProfileRepository.findByCityIgnoreCaseAndActiveTrue(city).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MuaProfileResponse> getTopArtists() {
        return muaProfileRepository.findTopArtists().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MuaProfileResponse updateProfile(UUID userId, MuaProfileRequest request) {
        MuaProfile profile = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        profile.setDisplayName(request.getDisplayName());
        profile.setBio(request.getBio());
        profile.setCity(request.getCity());
        profile.setLocality(request.getLocality());
        if (request.getPincode() != null) profile.setPincode(request.getPincode());
        if (request.getLatitude() != null) profile.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) profile.setLongitude(request.getLongitude());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());
        if (request.getCountryCode() != null) profile.setCountryCode(request.getCountryCode());
        if (request.getOccasions() != null) {
            profile.getOccasions().clear();
            profile.getOccasions().addAll(request.getOccasions());
        }
        if (request.getSkinToneExpertise() != null) {
            profile.getSkinToneExpertise().clear();
            profile.getSkinToneExpertise().addAll(request.getSkinToneExpertise());
        }
        if (request.getMinPrice() != null) profile.setMinPrice(request.getMinPrice());
        if (request.getMaxPrice() != null) profile.setMaxPrice(request.getMaxPrice());
        return toResponse(muaProfileRepository.save(profile));
    }

    @Transactional
    public PortfolioItemDto addPortfolioItem(UUID userId, PortfolioItemDto dto) {
        MuaProfile profile = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        PortfolioItem item = PortfolioItem.builder()
                .muaProfile(profile)
                .imageUrl(dto.getImageUrl())
                .caption(dto.getCaption())
                .occasion(dto.getOccasion())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        profile.getPortfolio().add(item);
        muaProfileRepository.save(profile);
        return toPortfolioDto(item);
    }

    @Transactional
    public MuaServiceDto addService(UUID userId, MuaServiceDto dto) {
        MuaProfile profile = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        MuaService service = MuaService.builder()
                .muaProfile(profile)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .durationMinutes(dto.getDurationMinutes())
                .occasion(dto.getOccasion())
                .category(dto.getCategory() != null ? dto.getCategory() : com.makeupseven.model.enums.ServiceCategory.MAKEUP)
                .build();
        profile.getServices().add(service);
        muaProfileRepository.save(profile);
        return toServiceDto(service);
    }

    public MuaProfile getEntity(UUID id) {
        return muaProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MUA not found"));
    }

    public MuaProfile getByUserId(UUID userId) {
        return muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
    }

    public MuaProfileResponse getProfileByUserId(UUID userId) {
        return toResponse(getByUserId(userId));
    }

    private MuaProfileResponse toResponse(MuaProfile p) {
        return MuaProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .displayName(p.getDisplayName())
                .bio(p.getBio())
                .city(p.getCity())
                .locality(p.getLocality())
                .pincode(p.getPincode())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .country(p.getCountry())
                .countryCode(p.getCountryCode())
                .occasions(p.getOccasions())
                .skinToneExpertise(p.getSkinToneExpertise())
                .minPrice(p.getMinPrice())
                .maxPrice(p.getMaxPrice())
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .totalBookings(p.getTotalBookings())
                .topArtist(p.getTopArtist())
                .verified(p.getVerified())
                .onboardingComplete(p.getOnboardingComplete())
                .featured(p.getFeatured())
                .responseTimeMinutes(p.getResponseTimeMinutes())
                .responseTimeLabel(formatResponseTime(p.getResponseTimeMinutes()))
                .subscriptionTier(p.getSubscriptionTier())
                .portfolio(p.getPortfolio().stream().map(this::toPortfolioDto).collect(Collectors.toList()))
                .services(p.getServices().stream().filter(MuaService::getActive).map(this::toServiceDto).collect(Collectors.toList()))
                .build();
    }

    static String formatResponseTime(Integer minutes) {
        if (minutes == null || minutes <= 60) return "Usually replies within an hour";
        if (minutes <= 120) return "Usually replies in 2 hours";
        if (minutes <= 240) return "Usually replies in 4 hours";
        return "Usually replies within a day";
    }

    private PortfolioItemDto toPortfolioDto(PortfolioItem item) {
        return PortfolioItemDto.builder()
                .id(item.getId())
                .imageUrl(item.getImageUrl())
                .caption(item.getCaption())
                .occasion(item.getOccasion())
                .sortOrder(item.getSortOrder())
                .build();
    }

    private MuaServiceDto toServiceDto(MuaService s) {
        return MuaServiceDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .price(s.getPrice())
                .durationMinutes(s.getDurationMinutes())
                .occasion(s.getOccasion())
                .category(s.getCategory())
                .build();
    }
}
