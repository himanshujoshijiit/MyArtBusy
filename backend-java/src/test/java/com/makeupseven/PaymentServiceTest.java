package com.makeupseven;

import com.makeupseven.model.Booking;
import com.makeupseven.model.enums.PaymentStatus;
import com.makeupseven.repository.BookingRepository;
import com.makeupseven.service.BookingService;
import com.makeupseven.service.ContractService;
import com.makeupseven.service.NotificationClient;
import com.makeupseven.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

  @Mock
  private BookingService bookingService;

  @Mock
  private NotificationClient notificationClient;

  @Mock
  private ContractService contractService;

  @InjectMocks
  private PaymentService paymentService;

  @Test
  void verifyPayment_usesRepositoryLookup_notFindAll() {
    String orderId = "order_test123";
    Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .razorpayOrderId(orderId)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

    when(bookingRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.of(booking));
    when(contractService.contractUrlFor(booking)).thenReturn("http://localhost/contracts/1");

    paymentService.verifyPayment(orderId, "pay_123", "mock");

    verify(bookingRepository).findByRazorpayOrderId(orderId);
    verify(bookingRepository, never()).findAll();
    verify(bookingRepository).save(booking);
  }

    @Test
    void verifyPayment_throwsWhenOrderNotFound() {
        when(bookingRepository.findByRazorpayOrderId("missing")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                paymentService.verifyPayment("missing", "pay_x", "mock"));
    }
}
