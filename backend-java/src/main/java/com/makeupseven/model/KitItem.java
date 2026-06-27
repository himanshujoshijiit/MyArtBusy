package com.makeupseven.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "kit_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mua_id", nullable = false)
    private MuaProfile muaProfile;

    @Column(nullable = false)
    private String name;

    private String brand;

    private String category;

    @Builder.Default
    private Integer quantity = 1;

    private LocalDate expiryDate;

    @Builder.Default
    private Boolean lowStockAlert = false;

    @Builder.Default
    private Integer minQuantity = 1;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
