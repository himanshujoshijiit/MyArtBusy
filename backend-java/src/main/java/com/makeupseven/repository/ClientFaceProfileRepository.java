package com.makeupseven.repository;

import com.makeupseven.model.ClientFaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientFaceProfileRepository extends JpaRepository<ClientFaceProfile, UUID> {
    List<ClientFaceProfile> findByMuaProfileId(UUID muaId);
    Optional<ClientFaceProfile> findByMuaProfileIdAndClientId(UUID muaId, UUID clientId);
}
