package com.makeupseven.repository;

import com.makeupseven.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {
    List<AvailabilitySlot> findByMuaProfileIdAndSlotDateBetweenAndAvailableTrueOrderBySlotDateAscStartTimeAsc(
        UUID muaId, LocalDate start, LocalDate end);
    List<AvailabilitySlot> findByMuaProfileIdAndSlotDateOrderByStartTimeAsc(UUID muaId, LocalDate date);
}
