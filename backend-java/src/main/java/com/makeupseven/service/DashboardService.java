package com.makeupseven.service;

import com.makeupseven.dto.*;
import com.makeupseven.model.*;
import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.model.enums.SubscriptionTier;
import com.makeupseven.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MuaProfileRepository muaProfileRepository;
    private final BookingRepository bookingRepository;
    private final KitItemRepository kitItemRepository;
    private final ClientFaceProfileRepository clientFaceProfileRepository;
    private final UserRepository userRepository;

    @Value("${makeupseven.free-tier-bookings:3}")
    private int freeTierBookings;

    public DashboardStatsResponse getStats(UUID userId) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));

        List<Booking> allBookings = bookingRepository.findByMuaProfileIdOrderByBookingDateDesc(mua.getId());
        long monthlyCount = bookingRepository.countMonthlyBookings(mua.getId());
        long pending = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();

        BigDecimal totalEarnings = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.DEPOSIT_PAID || b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        BigDecimal monthlyEarnings = allBookings.stream()
                .filter(b -> !b.getBookingDate().isBefore(monthStart))
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.DEPOSIT_PAID || b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.DEPOSIT_PAID || b.getStatus() == BookingStatus.CONFIRMED)
                .map(b -> b.getCommissionAmount() != null ? b.getCommissionAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netEarnings = totalEarnings.subtract(totalCommission);
        BigDecimal monthlyNet = monthlyEarnings.subtract(
                allBookings.stream()
                        .filter(b -> !b.getBookingDate().isBefore(monthStart))
                        .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.DEPOSIT_PAID || b.getStatus() == BookingStatus.CONFIRMED)
                        .map(b -> b.getCommissionAmount() != null ? b.getCommissionAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        List<KitAlertDto> alerts = new ArrayList<>();
        kitItemRepository.findExpiringSoon(mua.getId(), LocalDate.now().plusDays(30)).forEach(k ->
            alerts.add(KitAlertDto.builder().id(k.getId()).name(k.getName())
                .alertType("EXPIRY").message("Expires on " + k.getExpiryDate()).build()));
        kitItemRepository.findLowStock(mua.getId()).forEach(k ->
            alerts.add(KitAlertDto.builder().id(k.getId()).name(k.getName())
                .alertType("LOW_STOCK").message("Only " + k.getQuantity() + " left").build()));

        return DashboardStatsResponse.builder()
                .totalBookings(mua.getTotalBookings())
                .monthlyBookings((int) monthlyCount)
                .pendingBookings((int) pending)
                .totalEarnings(totalEarnings)
                .monthlyEarnings(monthlyEarnings)
                .netEarnings(netEarnings)
                .monthlyNetEarnings(monthlyNet)
                .totalCommission(totalCommission)
                .averageRating(mua.getRating())
                .reviewCount(mua.getReviewCount())
                .subscription(SubscriptionInfoDto.builder()
                        .tier(mua.getSubscriptionTier().name())
                        .bookingsUsed((int) monthlyCount)
                        .bookingsLimit(mua.getSubscriptionTier() == SubscriptionTier.PRO ? null : freeTierBookings)
                        .build())
                .kitAlerts(alerts)
                .build();
    }

    public List<ClientFaceProfileResponse> getClientProfiles(UUID userId) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        return clientFaceProfileRepository.findByMuaProfileId(mua.getId()).stream()
                .map(this::toClientProfile).collect(Collectors.toList());
    }

    @Transactional
    public ClientFaceProfileResponse saveClientProfile(UUID userId, ClientFaceProfileRequest request) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        User client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        ClientFaceProfile profile = clientFaceProfileRepository
                .findByMuaProfileIdAndClientId(mua.getId(), client.getId())
                .orElse(ClientFaceProfile.builder().muaProfile(mua).client(client).build());

        if (request.getSkinTone() != null) profile.setSkinTone(request.getSkinTone());
        if (request.getAllergies() != null) profile.setAllergies(request.getAllergies());
        if (request.getNotes() != null) profile.setNotes(request.getNotes());
        if (request.getPastLooks() != null) profile.setPastLooks(request.getPastLooks());

        return toClientProfile(clientFaceProfileRepository.save(profile));
    }

    public List<KitItemResponse> getKitItems(UUID userId) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        return kitItemRepository.findByMuaProfileId(mua.getId()).stream()
                .map(this::toKitItem).collect(Collectors.toList());
    }

    @Transactional
    public KitItemResponse addKitItem(UUID userId, KitItemRequest request) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        KitItem item = KitItem.builder()
                .muaProfile(mua)
                .name(request.getName())
                .brand(request.getBrand())
                .category(request.getCategory())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .expiryDate(request.getExpiryDate())
                .minQuantity(request.getMinQuantity() != null ? request.getMinQuantity() : 1)
                .build();
        return toKitItem(kitItemRepository.save(item));
    }

    private ClientFaceProfileResponse toClientProfile(ClientFaceProfile p) {
        return ClientFaceProfileResponse.builder()
                .id(p.getId())
                .clientId(p.getClient().getId())
                .clientName(p.getClient().getFullName())
                .skinTone(p.getSkinTone())
                .allergies(p.getAllergies())
                .notes(p.getNotes())
                .pastLooks(p.getPastLooks())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private KitItemResponse toKitItem(KitItem k) {
        return KitItemResponse.builder()
                .id(k.getId())
                .name(k.getName())
                .brand(k.getBrand())
                .category(k.getCategory())
                .quantity(k.getQuantity())
                .expiryDate(k.getExpiryDate())
                .lowStockAlert(k.getQuantity() <= k.getMinQuantity())
                .build();
    }
}
