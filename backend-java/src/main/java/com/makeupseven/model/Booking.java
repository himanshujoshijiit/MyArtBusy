package com.makeupseven.model;

import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.model.enums.Occasion;
import com.makeupseven.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mua_id", nullable = false)
    private MuaProfile muaProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private MuaService service;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private LocalTime startTime;

    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private Occasion occasion;

    @Column(length = 1000)
    private String notes;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    private String contractUrl;
    private String contractSignedAt;

    @Builder.Default
    private Boolean reviewRequested = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
