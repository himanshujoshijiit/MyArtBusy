package com.makeupseven.service;

import com.makeupseven.dto.*;
import com.makeupseven.model.*;
import com.makeupseven.model.enums.*;
import com.makeupseven.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MuaProfileRepository muaProfileRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository availabilityRepository;
    private final NotificationClient notificationClient;
    private final ContractService contractService;
    private final ClientFaceProfileRepository clientFaceProfileRepository;

    @Value("${makeupseven.deposit-rate:0.25}")
    private double depositRate;

    @Value("${makeupseven.commission-rate:0.10}")
    private double commissionRate;

    @Value("${makeupseven.free-tier-bookings:3}")
    private int freeTierBookings;

    @Transactional
    public BookingResponse createBooking(UUID clientId, CreateBookingRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        MuaProfile mua = muaProfileRepository.findById(request.getMuaId())
                .orElseThrow(() -> new RuntimeException("MUA not found"));

        if (mua.getSubscriptionTier() == SubscriptionTier.FREE) {
            long monthlyCount = bookingRepository.countMonthlyBookings(mua.getId());
            if (monthlyCount >= freeTierBookings) {
                throw new RuntimeException("This artist has reached their free tier booking limit. Upgrade to Pro required.");
            }
        }

        MuaService service = null;
        BigDecimal totalAmount;
        if (request.getServiceId() != null) {
            service = mua.getServices().stream()
                    .filter(s -> s.getId().equals(request.getServiceId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            totalAmount = service.getPrice();
        } else {
            totalAmount = mua.getMinPrice() != null ? mua.getMinPrice() : BigDecimal.valueOf(5000);
        }

        BigDecimal deposit = totalAmount.multiply(BigDecimal.valueOf(depositRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal commission = totalAmount.multiply(BigDecimal.valueOf(commissionRate)).setScale(2, RoundingMode.HALF_UP);

        LocalTime endTime = service != null && service.getDurationMinutes() != null
                ? request.getStartTime().plusMinutes(service.getDurationMinutes())
                : request.getStartTime().plusHours(2);

        Booking booking = Booking.builder()
                .client(client)
                .muaProfile(mua)
                .service(service)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .occasion(request.getOccasion())
                .notes(request.getNotes())
                .totalAmount(totalAmount)
                .depositAmount(deposit)
                .commissionAmount(commission)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);
        return toResponse(booking);
    }

    public BookingResponse getBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getClient().getId().equals(userId)
                && !booking.getMuaProfile().getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return toResponse(booking);
    }

    public List<BookingResponse> getClientBookings(UUID clientId) {
        return bookingRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<BookingResponse> getMuaBookings(UUID muaId) {
        return bookingRepository.findByMuaProfileIdOrderByBookingDateDesc(muaId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse confirmBooking(UUID bookingId, UUID muaUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getMuaProfile().getUser().getId().equals(muaUserId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (booking.getStatus() != BookingStatus.DEPOSIT_PAID && booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking cannot be confirmed in current status");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setContractUrl(contractService.contractUrlFor(booking));
        booking = bookingRepository.save(booking);
        notificationClient.sendBookingConfirmation(booking);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse completeBooking(UUID bookingId, UUID muaUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getMuaProfile().getUser().getId().equals(muaUserId)) {
            throw new RuntimeException("Unauthorized");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);

        MuaProfile mua = booking.getMuaProfile();
        mua.setTotalBookings(mua.getTotalBookings() + 1);
        muaProfileRepository.save(mua);

        ensureClientProfile(booking);
        notificationClient.sendReviewRequest(booking);
        booking.setReviewRequested(true);
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        boolean isClient = booking.getClient().getId().equals(userId);
        boolean isMua = booking.getMuaProfile().getUser().getId().equals(userId);
        if (!isClient && !isMua) throw new RuntimeException("Unauthorized");
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking cannot be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        releaseSlot(booking);
        bookingRepository.save(booking);
        return toResponse(booking);
    }

    public List<AvailabilitySlotDto> getAvailability(UUID muaId, LocalDate start, LocalDate end) {
        return availabilityRepository
                .findByMuaProfileIdAndSlotDateBetweenAndAvailableTrueOrderBySlotDateAscStartTimeAsc(muaId, start, end)
                .stream().map(this::toSlotDto).collect(Collectors.toList());
    }

    @Transactional
    public AvailabilitySlotDto addAvailability(UUID userId, AvailabilitySlotDto dto) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        AvailabilitySlot slot = AvailabilitySlot.builder()
                .muaProfile(mua)
                .slotDate(dto.getSlotDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .available(true)
                .booked(false)
                .build();
        return toSlotDto(availabilityRepository.save(slot));
    }

    @Transactional
    public void markSlotBookedOnPayment(Booking booking) {
        markSlotBooked(booking.getMuaProfile().getId(), booking.getBookingDate(), booking.getStartTime());
    }

    private void ensureClientProfile(Booking booking) {
        clientFaceProfileRepository.findByMuaProfileIdAndClientId(
                booking.getMuaProfile().getId(), booking.getClient().getId()
        ).orElseGet(() -> clientFaceProfileRepository.save(
                ClientFaceProfile.builder()
                        .muaProfile(booking.getMuaProfile())
                        .client(booking.getClient())
                        .notes("Auto-created from booking on " + booking.getBookingDate())
                        .build()
        ));
    }

    private void markSlotBooked(UUID muaId, LocalDate date, LocalTime startTime) {
        availabilityRepository.findByMuaProfileIdAndSlotDateOrderByStartTimeAsc(muaId, date).stream()
                .filter(s -> s.getStartTime().equals(startTime))
                .findFirst()
                .ifPresent(slot -> {
                    slot.setBooked(true);
                    slot.setAvailable(false);
                    availabilityRepository.save(slot);
                });
    }

    private void releaseSlot(Booking booking) {
        availabilityRepository.findByMuaProfileIdAndSlotDateOrderByStartTimeAsc(
                booking.getMuaProfile().getId(), booking.getBookingDate()).stream()
                .filter(s -> s.getStartTime().equals(booking.getStartTime()))
                .findFirst()
                .ifPresent(slot -> {
                    slot.setBooked(false);
                    slot.setAvailable(true);
                    availabilityRepository.save(slot);
                });
    }

    private BookingResponse toResponse(Booking b) {
        BigDecimal remaining = b.getTotalAmount().subtract(
                b.getDepositAmount() != null ? b.getDepositAmount() : BigDecimal.ZERO);
        return BookingResponse.builder()
                .id(b.getId())
                .clientId(b.getClient().getId())
                .clientName(b.getClient().getFullName())
                .muaId(b.getMuaProfile().getId())
                .muaName(b.getMuaProfile().getDisplayName())
                .serviceId(b.getService() != null ? b.getService().getId() : null)
                .serviceName(b.getService() != null ? b.getService().getName() : null)
                .bookingDate(b.getBookingDate())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .occasion(b.getOccasion())
                .notes(b.getNotes())
                .totalAmount(b.getTotalAmount())
                .depositAmount(b.getDepositAmount())
                .commissionAmount(b.getCommissionAmount())
                .remainingAmount(remaining)
                .status(b.getStatus())
                .paymentStatus(b.getPaymentStatus())
                .razorpayOrderId(b.getRazorpayOrderId())
                .contractUrl(b.getContractUrl() != null ? b.getContractUrl() : contractService.contractUrlFor(b))
                .contractSignedAt(b.getContractSignedAt())
                .contractSigned(b.getContractSignedAt() != null)
                .createdAt(b.getCreatedAt())
                .build();
    }

    private AvailabilitySlotDto toSlotDto(AvailabilitySlot s) {
        return AvailabilitySlotDto.builder()
                .id(s.getId())
                .slotDate(s.getSlotDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .available(s.getAvailable() && !s.getBooked())
                .build();
    }
}
