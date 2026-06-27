package com.makeupseven;

import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.PaymentStatus;
import com.makeupseven.repository.BookingRepository;
import com.makeupseven.service.RefundService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RefundService refundService;

    @Test
    void refundsFullDepositWhenCancelledMoreThan48HoursBefore() {
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .paymentStatus(PaymentStatus.DEPOSIT_PAID)
                .depositAmount(new BigDecimal("2500"))
                .bookingDate(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(10, 0))
                .build();

        BigDecimal refund = refundService.processCancellationRefund(booking);

        assertEquals(new BigDecimal("2500"), refund);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void forfeitsDepositWhenCancelledWithin48Hours() {
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .paymentStatus(PaymentStatus.DEPOSIT_PAID)
                .depositAmount(new BigDecimal("2500"))
                .bookingDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .build();

        BigDecimal refund = refundService.processCancellationRefund(booking);

        assertEquals(BigDecimal.ZERO, refund);
    }
}
