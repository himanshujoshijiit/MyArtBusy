package com.makeupseven.repository;

import com.makeupseven.model.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, UUID> {
    List<BlockedDate> findByMuaProfileIdOrderByBlockDateAsc(UUID muaId);
    boolean existsByMuaProfileIdAndBlockDate(UUID muaId, LocalDate blockDate);
    void deleteByMuaProfileIdAndBlockDate(UUID muaId, LocalDate blockDate);
}
