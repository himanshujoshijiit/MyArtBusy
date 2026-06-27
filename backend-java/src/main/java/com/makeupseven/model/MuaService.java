package com.makeupseven.model;

import com.makeupseven.model.enums.Occasion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "mua_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuaService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mua_id", nullable = false)
    private MuaProfile muaProfile;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private Occasion occasion;

    @Builder.Default
    private Boolean active = true;
}
