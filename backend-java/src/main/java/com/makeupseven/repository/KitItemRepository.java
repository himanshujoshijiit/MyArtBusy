package com.makeupseven.repository;

import com.makeupseven.model.KitItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface KitItemRepository extends JpaRepository<KitItem, UUID> {
    List<KitItem> findByMuaProfileId(UUID muaId);

    @Query("SELECT k FROM KitItem k WHERE k.muaProfile.id = :muaId AND k.expiryDate <= :date")
    List<KitItem> findExpiringSoon(UUID muaId, LocalDate date);

    @Query("SELECT k FROM KitItem k WHERE k.muaProfile.id = :muaId AND k.quantity <= k.minQuantity")
    List<KitItem> findLowStock(UUID muaId);
}
