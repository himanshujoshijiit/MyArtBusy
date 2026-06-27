package com.makeupseven.repository;

import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<Booking> findByMuaProfileIdOrderByBookingDateDesc(UUID muaId);

    @Query(value = "SELECT COUNT(*) FROM bookings b WHERE b.mua_id = CAST(:muaId AS uuid) " +
           "AND b.status NOT IN ('CANCELLED') " +
           "AND DATE_TRUNC('month', b.created_at) = DATE_TRUNC('month', CURRENT_TIMESTAMP)",
           nativeQuery = true)
    long countMonthlyBookings(@Param("muaId") UUID muaId);

    List<Booking> findByMuaProfileIdAndBookingDateBetween(UUID muaId, LocalDate start, LocalDate end);

    List<Booking> findByStatusAndReviewRequestedFalse(BookingStatus status);
}
