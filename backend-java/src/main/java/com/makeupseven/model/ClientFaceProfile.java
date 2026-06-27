package com.makeupseven.model;

import com.makeupseven.model.enums.SkinTone;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client_face_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientFaceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mua_id", nullable = false)
    private MuaProfile muaProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Enumerated(EnumType.STRING)
    private SkinTone skinTone;

    @Column(length = 500)
    private String allergies;

    @Column(length = 1000)
    private String notes;

    @ElementCollection
    @CollectionTable(name = "client_past_looks", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "look_url")
    @Builder.Default
    private List<String> pastLooks = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
