package com.makeupseven.service;

import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.repository.BookingRepository;
import com.makeupseven.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 30 12 * * *")
    public void sendAppointmentReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Booking> upcoming = bookingRepository.findUpcomingForReminder(tomorrow);
        for (Booking booking : upcoming) {
            try {
                notificationClient.sendAppointmentReminder(booking);
                log.info("Sent reminder for booking {}", booking.getId());
            } catch (Exception e) {
                log.warn("Reminder failed for {}: {}", booking.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void sendReviewReminders() {
        Instant dayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        List<Booking> completed = bookingRepository.findByStatusAndReviewRequestedFalse(BookingStatus.COMPLETED);
        for (Booking booking : completed) {
            if (booking.getUpdatedAt() != null && booking.getUpdatedAt().isBefore(dayAgo)
                    && !reviewRepository.existsByBookingId(booking.getId())) {
                try {
                    notificationClient.sendReviewRequest(booking);
                    booking.setReviewRequested(true);
                    bookingRepository.save(booking);
                } catch (Exception e) {
                    log.warn("Review reminder failed: {}", e.getMessage());
                }
            }
        }
    }
}
