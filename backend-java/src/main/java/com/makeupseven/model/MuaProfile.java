package com.makeupseven.model;

import com.makeupseven.model.enums.Occasion;
import com.makeupseven.model.enums.SkinTone;
import com.makeupseven.model.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "mua_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuaProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String displayName;

    @Column(length = 2000)
    private String bio;

    @Column(nullable = false)
    private String city;

    private String locality;

    private String country;

    @Builder.Default
    private String countryCode = "IN";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mua_occasions", joinColumns = @JoinColumn(name = "mua_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "occasion")
    @Builder.Default
    private Set<Occasion> occasions = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mua_skin_tones", joinColumns = @JoinColumn(name = "mua_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "skin_tone")
    @Builder.Default
    private Set<SkinTone> skinToneExpertise = new HashSet<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    @Builder.Default
    private Integer totalBookings = 0;

    @Builder.Default
    private Integer monthlyBookings = 0;

    @Builder.Default
    private Boolean topArtist = false;

    @Builder.Default
    private Boolean verified = false;

    @Builder.Default
    private Boolean featured = false;

    @Builder.Default
    private Integer responseTimeMinutes = 120;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    private Instant subscriptionExpiresAt;

    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "muaProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PortfolioItem> portfolio = new ArrayList<>();

    @OneToMany(mappedBy = "muaProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MuaService> services = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
