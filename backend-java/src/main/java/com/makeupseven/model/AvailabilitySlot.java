package com.makeupseven.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "availability_slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"mua_id", "slot_date", "start_time"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mua_id", nullable = false)
    private MuaProfile muaProfile;

    @Column(nullable = false)
    private LocalDate slotDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Builder.Default
    private Boolean available = true;

    @Builder.Default
    private Boolean booked = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
