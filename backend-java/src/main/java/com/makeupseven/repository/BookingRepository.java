package com.makeupseven.repository;

import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    List<Booking> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<Booking> findByMuaProfileIdOrderByBookingDateDesc(UUID muaId);

    @Query(value = "SELECT COUNT(*) FROM bookings b WHERE b.mua_id = CAST(:muaId AS uuid) " +
           "AND b.status NOT IN ('CANCELLED') " +
           "AND DATE_TRUNC('month', b.created_at) = DATE_TRUNC('month', CURRENT_TIMESTAMP)",
           nativeQuery = true)
    long countMonthlyBookings(@Param("muaId") UUID muaId);

    List<Booking> findByMuaProfileIdAndBookingDateBetween(UUID muaId, LocalDate start, LocalDate end);

    List<Booking> findByStatusAndReviewRequestedFalse(BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate = :tomorrow " +
           "AND b.status IN (com.makeupseven.model.enums.BookingStatus.CONFIRMED, " +
           "com.makeupseven.model.enums.BookingStatus.DEPOSIT_PAID)")
    List<Booking> findUpcomingForReminder(@Param("tomorrow") LocalDate tomorrow);

    @Query("SELECT b FROM Booking b WHERE b.status = com.makeupseven.model.enums.BookingStatus.COMPLETED " +
           "AND b.reviewRequested = true AND b.updatedAt < CURRENT_TIMESTAMP - 1 DAY")
    List<Booking> findPendingReviewReminders();
}
