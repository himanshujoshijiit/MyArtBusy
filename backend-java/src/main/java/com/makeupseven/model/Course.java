package com.makeupseven.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String instructorName;

    private String instructorId;

    private String thumbnailUrl;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    private Integer durationHours;

    private String level;

    @Builder.Default
    private Boolean published = true;

    @Builder.Default
    private Integer enrollmentCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
