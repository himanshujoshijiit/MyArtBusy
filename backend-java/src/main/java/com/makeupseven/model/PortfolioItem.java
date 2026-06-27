package com.makeupseven.model;

import com.makeupseven.model.enums.Occasion;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "portfolio_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mua_id", nullable = false)
    private MuaProfile muaProfile;

    @Column(nullable = false)
    private String imageUrl;

    private String caption;

    @Enumerated(EnumType.STRING)
    private Occasion occasion;

    @Builder.Default
    private Integer sortOrder = 0;
}
