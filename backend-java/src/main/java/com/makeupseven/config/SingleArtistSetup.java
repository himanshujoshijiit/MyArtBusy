package com.makeupseven.config;

import com.makeupseven.model.*;
import com.makeupseven.model.enums.*;
import com.makeupseven.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Ensures single-artist studio mode: one owner profile (Priya Prachi), other demo MUAs deactivated.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SingleArtistSetup implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MuaProfileRepository muaProfileRepository;
    private final AvailabilitySlotRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${makeupseven.single-artist-mode:true}")
    private boolean singleArtistMode;

    @Value("${makeupseven.owner-email:priya@priyaprachi.com}")
    private String ownerEmail;

    public static final String OWNER_PASSWORD = "priya123";
    public static final String DISPLAY_NAME = "Priya Prachi — Makeup & Beauty Studio";

    @Override
    @Transactional
    public void run(String... args) {
        if (!singleArtistMode) {
            return;
        }
        ensureOwnerStudio();
    }

    private void ensureOwnerStudio() {
        User owner = userRepository.findByEmail(ownerEmail).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(ownerEmail)
                        .passwordHash(passwordEncoder.encode(OWNER_PASSWORD))
                        .fullName("Priya Prachi")
                        .phone("9876543210")
                        .role(UserRole.MUA)
                        .avatarUrl("https://images.unsplash.com/photo-1596462502278-27bfdd403348?w=400")
                        .build()));

        MuaProfile profile = muaProfileRepository.findByUserId(owner.getId()).orElse(null);
        if (profile == null) {
            profile = buildOwnerProfile(owner);
            addPortfolio(profile);
            addServices(profile);
            profile = muaProfileRepository.save(profile);
            seedAvailability(profile);
            log.info("Created owner studio profile for {}", ownerEmail);
        } else {
            profile.setDisplayName(DISPLAY_NAME);
            profile.setBio(ownerBio());
            profile.setActive(true);
            profile.setVerified(true);
            profile.setTopArtist(true);
            profile.setFeatured(true);
            profile.setOnboardingComplete(true);
            profile.setSubscriptionTier(SubscriptionTier.PRO);
            profile.setCity("Bengaluru");
            profile.setLocality("Koramangala");
            profile.setPincode("560034");
            profile.setLatitude(12.9352);
            profile.setLongitude(77.6245);
            if (profile.getPortfolio().isEmpty()) {
                addPortfolio(profile);
            }
            if (profile.getServices().isEmpty()) {
                addServices(profile);
            }
            muaProfileRepository.save(profile);
            log.info("Updated owner studio profile for {}", ownerEmail);
        }

        final var ownerProfileId = profile.getId();
        muaProfileRepository.findAll().stream()
                .filter(p -> !p.getId().equals(ownerProfileId))
                .forEach(p -> {
                    p.setActive(false);
                    muaProfileRepository.save(p);
                });

        log.info("Single-artist mode: {} is the active studio (login: {} / {})",
                DISPLAY_NAME, ownerEmail, OWNER_PASSWORD);
    }

    private MuaProfile buildOwnerProfile(User owner) {
        return MuaProfile.builder()
                .user(owner)
                .displayName(DISPLAY_NAME)
                .bio(ownerBio())
                .city("Bengaluru")
                .locality("Koramangala")
                .pincode("560034")
                .latitude(12.9352)
                .longitude(77.6245)
                .country("India")
                .countryCode("IN")
                .occasions(Set.of(
                        Occasion.BRIDAL, Occasion.WEDDING, Occasion.HALDI_MEHENDI,
                        Occasion.PARTY, Occasion.GLAMOROUS, Occasion.ENGAGEMENT, Occasion.RECEPTION))
                .skinToneExpertise(Set.of(SkinTone.FAIR, SkinTone.MEDIUM, SkinTone.OLIVE, SkinTone.DEEP, SkinTone.ALL))
                .minPrice(new BigDecimal("3500"))
                .maxPrice(new BigDecimal("22000"))
                .rating(5.0)
                .reviewCount(0)
                .totalBookings(0)
                .topArtist(true)
                .verified(true)
                .featured(true)
                .onboardingComplete(true)
                .responseTimeMinutes(60)
                .subscriptionTier(SubscriptionTier.PRO)
                .active(true)
                .build();
    }

    private static String ownerBio() {
        return "Professional makeup artist based in Bengaluru. Specializing in bridal, Haldi & Mehendi, "
                + "party glam, and HD makeup. Rich experience crafting flawless looks across Bihar and Karnataka — "
                + "now booking home visits across Bengaluru. Premium products, personalized consultations, "
                + "and looks that last all day.";
    }

    private void addPortfolio(MuaProfile profile) {
        String[][] items = {
                {"https://images.unsplash.com/photo-1519699047748-de8e457a634e?w=800", "Bridal glow", "BRIDAL"},
                {"https://images.unsplash.com/photo-1487412940907-5a55ae5e4c6b?w=800", "Haldi radiance", "HALDI_MEHENDI"},
                {"https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=800", "Party glam", "PARTY"},
                {"https://images.unsplash.com/photo-1596704017258-9b9c606462aa?w=800", "Engagement look", "ENGAGEMENT"},
                {"https://images.unsplash.com/photo-1485893086445-ed758652e607?w=800", "Reception elegance", "RECEPTION"},
                {"https://images.unsplash.com/photo-1509963188294-7b1a109d17e8?w=800", "HD flawless base", "GLAMOROUS"},
        };
        for (int i = 0; i < items.length; i++) {
            profile.getPortfolio().add(PortfolioItem.builder()
                    .muaProfile(profile)
                    .imageUrl(items[i][0])
                    .caption(items[i][1])
                    .occasion(Occasion.valueOf(items[i][2]))
                    .sortOrder(i)
                    .build());
        }
    }

    private void addServices(MuaProfile profile) {
        profile.getServices().add(MuaService.builder().muaProfile(profile)
                .name("Bridal Makeup — HD").description("Full bridal look with HD base, lashes & setting")
                .price(new BigDecimal("18000")).durationMinutes(180).occasion(Occasion.BRIDAL)
                .category(ServiceCategory.MAKEUP).build());
        profile.getServices().add(MuaService.builder().muaProfile(profile)
                .name("Haldi & Mehendi Makeup").description("Fresh, dewy look for Haldi/Mehendi ceremonies")
                .price(new BigDecimal("6500")).durationMinutes(120).occasion(Occasion.HALDI_MEHENDI)
                .category(ServiceCategory.MAKEUP).build());
        profile.getServices().add(MuaService.builder().muaProfile(profile)
                .name("Party / Glam Makeup").description("Evening glam — smoky eyes, contour, long-wear")
                .price(new BigDecimal("4500")).durationMinutes(90).occasion(Occasion.PARTY)
                .category(ServiceCategory.MAKEUP).build());
        profile.getServices().add(MuaService.builder().muaProfile(profile)
                .name("Engagement Makeup").description("Camera-ready engagement ceremony look")
                .price(new BigDecimal("8500")).durationMinutes(120).occasion(Occasion.ENGAGEMENT)
                .category(ServiceCategory.MAKEUP).build());
        profile.getServices().add(MuaService.builder().muaProfile(profile)
                .name("Threading").description("Eyebrow & face threading")
                .price(new BigDecimal("200")).durationMinutes(30).category(ServiceCategory.SALON).build());
        profile.getServices().add(MuaService.builder().muaProfile(profile)
                .name("Facial (Pre-makeup prep)").description("Deep cleanse & glow prep before makeup")
                .price(new BigDecimal("800")).durationMinutes(45).category(ServiceCategory.SALON).build());
    }

    private void seedAvailability(MuaProfile profile) {
        LocalDate today = LocalDate.now();
        for (int d = 1; d <= 21; d++) {
            LocalDate date = today.plusDays(d);
            if (date.getDayOfWeek().getValue() == 7) continue;
            for (int h = 9; h <= 17; h += 2) {
                availabilityRepository.save(AvailabilitySlot.builder()
                        .muaProfile(profile)
                        .slotDate(date)
                        .startTime(LocalTime.of(h, 0))
                        .endTime(LocalTime.of(h + 2, 0))
                        .available(true)
                        .booked(false)
                        .build());
            }
        }
    }
}
