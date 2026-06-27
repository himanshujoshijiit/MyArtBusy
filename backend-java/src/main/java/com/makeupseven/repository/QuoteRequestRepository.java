package com.makeupseven.repository;

import com.makeupseven.model.QuoteRequest;
import com.makeupseven.model.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, UUID> {
    List<QuoteRequest> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<QuoteRequest> findByMuaProfileIdOrderByCreatedAtDesc(UUID muaId);
    List<QuoteRequest> findByMuaProfileIdAndStatusOrderByCreatedAtDesc(UUID muaId, QuoteStatus status);
}
