package com.makeupseven.repository;

import com.makeupseven.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByMuaProfileIdOrderByCreatedAtDesc(UUID muaId);
    Optional<Review> findByBookingId(UUID bookingId);
    boolean existsByBookingId(UUID bookingId);
}
