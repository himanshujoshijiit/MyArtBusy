package com.makeupseven.config;

import com.makeupseven.model.*;
import com.makeupseven.model.enums.*;
import com.makeupseven.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MuaProfileRepository muaProfileRepository;
    private final AvailabilitySlotRepository availabilityRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            if (courseRepository.count() == 0) {
                log.info("Seeding courses...");
                seedCourses();
            }
            ensureAdminUser();
            log.info("Database already seeded, skipping");
            return;
        }
        log.info("Seeding demo data for MakeupSeven Bengaluru launch...");

        seedMua("Priya Sharma", "priya@makeupseven.com", "9876543210",
                "Priya Sharma — Bridal Specialist",
                "Award-winning bridal makeup artist with 8+ years experience. Specializing in South Indian bridal looks, HD makeup, and airbrush techniques. Featured in Vogue India.",
                "Bengaluru", "Indiranagar",
                Set.of(Occasion.BRIDAL, Occasion.WEDDING, Occasion.ENGAGEMENT, Occasion.RECEPTION),
                Set.of(SkinTone.FAIR, SkinTone.MEDIUM, SkinTone.OLIVE, SkinTone.DEEP),
                new BigDecimal("8000"), new BigDecimal("25000"),
                4.9, 47, 156, true, true, true, 45, SubscriptionTier.PRO);

        seedMua("Ananya Reddy", "ananya@makeupseven.com", "9876543211",
                "Ananya Reddy Makeup Studio",
                "Editorial and party makeup specialist. Known for bold, creative looks and flawless base work.",
                "Bengaluru", "Koramangala",
                Set.of(Occasion.PARTY, Occasion.EDITORIAL, Occasion.PERSONAL_EVENT),
                Set.of(SkinTone.ALL),
                new BigDecimal("3000"), new BigDecimal("12000"),
                4.7, 32, 89, true, true, false, 90, SubscriptionTier.PRO);

        seedMua("Meera Nair", "meera@makeupseven.com", "9876543212",
                "Meera Nair — Film & TV",
                "Film and television makeup artist. Worked on 15+ Kannada and Tamil productions.",
                "Bengaluru", "Whitefield",
                Set.of(Occasion.FILM, Occasion.EDITORIAL),
                Set.of(SkinTone.MEDIUM, SkinTone.TAN, SkinTone.DEEP, SkinTone.DARK),
                new BigDecimal("5000"), new BigDecimal("20000"),
                4.8, 21, 64, true, true, false, 120, SubscriptionTier.FREE);

        seedMua("Kavya Iyer", "kavya@makeupseven.com", "9876543213",
                "Kavya Iyer Beauty",
                "Affordable bridal and party makeup. Home visits across Bengaluru.",
                "Bengaluru", "Jayanagar",
                Set.of(Occasion.BRIDAL, Occasion.PARTY, Occasion.PERSONAL_EVENT),
                Set.of(SkinTone.FAIR, SkinTone.LIGHT, SkinTone.MEDIUM),
                new BigDecimal("2500"), new BigDecimal("8000"),
                4.5, 18, 42, false, true, false, 180, SubscriptionTier.FREE);

        seedMua("Divya Menon", "divya@makeupseven.com", "9876543214",
                "Divya Menon — Luxury Bridal",
                "Luxury bridal makeup for destination weddings. Premium products only — Charlotte Tilbury, Pat McGrath, Dior.",
                "Bengaluru", "HSR Layout",
                Set.of(Occasion.BRIDAL, Occasion.WEDDING, Occasion.RECEPTION),
                Set.of(SkinTone.FAIR, SkinTone.LIGHT, SkinTone.MEDIUM, SkinTone.OLIVE),
                new BigDecimal("15000"), new BigDecimal("50000"),
                5.0, 12, 38, true, true, true, 30, SubscriptionTier.PRO);

        User demoClient = User.builder()
                .email("demo@makeupseven.com")
                .passwordHash(passwordEncoder.encode("demo123"))
                .fullName("Demo Client")
                .phone("9999888877")
                .role(UserRole.CLIENT)
                .build();
        userRepository.save(demoClient);

        User admin = User.builder()
                .email("admin@makeupseven.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("Platform Admin")
                .phone("9000000000")
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);

        seedCourses();

        log.info("Seeded 5 MUAs + demo client + admin (admin@makeupseven.com / admin123)");
    }

    private void ensureAdminUser() {
        if (!userRepository.existsByEmail("admin@makeupseven.com")) {
            userRepository.save(User.builder()
                    .email("admin@makeupseven.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .fullName("Platform Admin")
                    .phone("9000000000")
                    .role(UserRole.ADMIN)
                    .build());
            log.info("Created admin user: admin@makeupseven.com / admin123");
        }
    }

    private void seedCourses() {
        courseRepository.save(Course.builder()
                .title("Bridal Makeup Masterclass")
                .description("Complete bridal makeup course covering South Indian, North Indian, and fusion bridal looks. Learn HD base, airbrush, and long-lasting techniques from a Vogue-featured artist.")
                .instructorName("Priya Sharma")
                .thumbnailUrl("https://images.unsplash.com/photo-1519699047748-de8e457a634e?w=600")
                .price(new BigDecimal("4999"))
                .durationHours(12)
                .level("Intermediate")
                .enrollmentCount(234)
                .build());
        courseRepository.save(Course.builder()
                .title("Editorial & Creative Makeup")
                .description("Push creative boundaries with editorial techniques — cut crease, graphic liner, avant-garde looks for photoshoots and fashion.")
                .instructorName("Ananya Reddy")
                .thumbnailUrl("https://images.unsplash.com/photo-1485893086445-ed758652e607?w=600")
                .price(new BigDecimal("3499"))
                .durationHours(8)
                .level("Advanced")
                .enrollmentCount(89)
                .build());
        courseRepository.save(Course.builder()
                .title("Makeup Basics for Beginners")
                .description("Start your MUA career — skin prep, colour theory, everyday glam, and building your first kit on a budget.")
                .instructorName("Priya Sharma")
                .thumbnailUrl("https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600")
                .price(new BigDecimal("1999"))
                .durationHours(6)
                .level("Beginner")
                .enrollmentCount(512)
                .build());
    }

    private void seedMua(String name, String email, String phone, String displayName, String bio,
                         String city, String locality, Set<Occasion> occasions, Set<SkinTone> skinTones,
                         BigDecimal minPrice, BigDecimal maxPrice, double rating, int reviews, int bookings,
                         boolean topArtist, boolean verified, boolean featured, int responseMins,
                         SubscriptionTier tier) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("artist123"))
                .fullName(name)
                .phone(phone)
                .role(UserRole.MUA)
                .avatarUrl("https://images.unsplash.com/photo-1596462502278-27bfdd403348?w=400")
                .build();
        user = userRepository.save(user);

        MuaProfile profile = MuaProfile.builder()
                .user(user)
                .displayName(displayName)
                .bio(bio)
                .city(city)
                .locality(locality)
                .country("India")
                .countryCode("IN")
                .occasions(occasions)
                .skinToneExpertise(skinTones)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .rating(rating)
                .reviewCount(reviews)
                .totalBookings(bookings)
                .topArtist(topArtist)
                .verified(verified)
                .featured(featured)
                .responseTimeMinutes(responseMins)
                .subscriptionTier(tier)
                .onboardingComplete(true)
                .pincode(localityPincode(locality))
                .latitude(localityLat(locality))
                .longitude(localityLng(locality))
                .active(true)
                .build();

        addPortfolio(profile, occasions);
        addServices(profile, occasions, minPrice, maxPrice);

        profile = muaProfileRepository.save(profile);
        seedAvailability(profile);
    }

    private void addPortfolio(MuaProfile profile, Set<Occasion> occasions) {
        String[] images = {
            "https://images.unsplash.com/photo-1487412940907-5a55ae5e4c6b?w=600",
            "https://images.unsplash.com/photo-1519699047748-de8e457a634e?w=600",
            "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600",
            "https://images.unsplash.com/photo-1596704017258-9b9c606462aa?w=600",
            "https://images.unsplash.com/photo-1485893086445-ed758652e607?w=600",
            "https://images.unsplash.com/photo-1509963188294-7b1a109d17e8?w=600"
        };
        Occasion[] occasionArr = occasions.toArray(new Occasion[0]);
        for (int i = 0; i < images.length; i++) {
            profile.getPortfolio().add(PortfolioItem.builder()
                    .muaProfile(profile)
                    .imageUrl(images[i])
                    .caption("Look " + (i + 1))
                    .occasion(occasionArr[i % occasionArr.length])
                    .sortOrder(i)
                    .build());
        }
    }

    private void addServices(MuaProfile profile, Set<Occasion> occasions, BigDecimal min, BigDecimal max) {
        if (occasions.contains(Occasion.BRIDAL) || occasions.contains(Occasion.WEDDING)) {
            profile.getServices().add(MuaService.builder().muaProfile(profile)
                    .name("Bridal Makeup").description("Full bridal look with hair styling")
                    .price(max).durationMinutes(180).occasion(Occasion.BRIDAL).build());
            profile.getServices().add(MuaService.builder().muaProfile(profile)
                    .name("Engagement Makeup").description("Engagement ceremony look")
                    .price(min.multiply(new BigDecimal("1.5"))).durationMinutes(120).occasion(Occasion.ENGAGEMENT).build());
        }
        if (occasions.contains(Occasion.PARTY)) {
            profile.getServices().add(MuaService.builder().muaProfile(profile)
                    .name("Party Makeup").description("Glam party look")
                    .price(min).durationMinutes(90).occasion(Occasion.PARTY).build());
        }
        if (occasions.contains(Occasion.EDITORIAL) || occasions.contains(Occasion.FILM)) {
            profile.getServices().add(MuaService.builder().muaProfile(profile)
                    .name("Editorial / Film Look").description("Creative editorial makeup")
                    .price(max.multiply(new BigDecimal("0.8"))).durationMinutes(120).occasion(Occasion.EDITORIAL).build());
        }
        if (profile.getServices().isEmpty()) {
            profile.getServices().add(MuaService.builder().muaProfile(profile)
                    .name("Makeup Session").description("Professional makeup session")
                    .price(min).durationMinutes(90).occasion(occasions.iterator().next()).build());
        }
    }

    private String localityPincode(String locality) {
        return switch (locality) {
            case "Indiranagar" -> "560038";
            case "Koramangala" -> "560034";
            case "Whitefield" -> "560066";
            case "Jayanagar" -> "560041";
            case "HSR Layout" -> "560102";
            default -> "560001";
        };
    }

    private double localityLat(String locality) {
        return switch (locality) {
            case "Indiranagar" -> 12.9784;
            case "Koramangala" -> 12.9352;
            case "Whitefield" -> 12.9698;
            case "Jayanagar" -> 12.9250;
            case "HSR Layout" -> 12.9116;
            default -> 12.9716;
        };
    }

    private double localityLng(String locality) {
        return switch (locality) {
            case "Indiranagar" -> 77.6408;
            case "Koramangala" -> 77.6245;
            case "Whitefield" -> 77.7499;
            case "Jayanagar" -> 77.5938;
            case "HSR Layout" -> 77.6388;
            default -> 77.5946;
        };
    }

    private void seedAvailability(MuaProfile profile) {
        LocalDate today = LocalDate.now();
        for (int d = 1; d <= 14; d++) {
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
