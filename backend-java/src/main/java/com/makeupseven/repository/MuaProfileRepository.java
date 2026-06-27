package com.makeupseven.repository;

import com.makeupseven.model.MuaProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MuaProfileRepository extends JpaRepository<MuaProfile, UUID> {
    Optional<MuaProfile> findByUserId(UUID userId);
    List<MuaProfile> findByCityIgnoreCaseAndActiveTrue(String city);
    List<MuaProfile> findByActiveTrue();

    @Query("SELECT m FROM MuaProfile m WHERE m.active = true AND m.topArtist = true ORDER BY m.rating DESC")
    List<MuaProfile> findTopArtists();
}
