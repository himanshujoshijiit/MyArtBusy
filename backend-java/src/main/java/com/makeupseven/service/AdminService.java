package com.makeupseven.service;

import com.makeupseven.dto.AdminMuaDto;
import com.makeupseven.dto.AdminStatsDto;
import com.makeupseven.model.Booking;
import com.makeupseven.model.MuaProfile;
import com.makeupseven.model.User;
import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.model.enums.UserRole;
import com.makeupseven.repository.BookingRepository;
import com.makeupseven.repository.MuaProfileRepository;
import com.makeupseven.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MuaProfileRepository muaProfileRepository;
    private final BookingRepository bookingRepository;

    public AdminStatsDto getStats() {
        List<MuaProfile> allMuas = muaProfileRepository.findAll();
        List<MuaProfile> pending = allMuas.stream()
                .filter(m -> !m.getVerified() || !Boolean.TRUE.equals(m.getOnboardingComplete()))
                .collect(Collectors.toList());
        List<Booking> bookings = bookingRepository.findAll();
        BigDecimal revenue = bookings.stream()
                .filter(b -> b.getPaymentStatus() != null && b.getCommissionAmount() != null)
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(Booking::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminStatsDto.builder()
                .totalUsers(userRepository.count())
                .totalMuas(allMuas.size())
                .pendingVerification(pending.size())
                .totalBookings(bookings.size())
                .totalRevenue(revenue)
                .activeBookings(bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                                || b.getStatus() == BookingStatus.DEPOSIT_PAID)
                        .count())
                .pendingMuas(pending.stream().map(m -> AdminMuaDto.builder()
                        .id(m.getId().toString())
                        .displayName(m.getDisplayName())
                        .email(m.getUser().getEmail())
                        .city(m.getCity())
                        .onboardingComplete(Boolean.TRUE.equals(m.getOnboardingComplete()))
                        .build()).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void verifyMua(UUID muaId) {
        MuaProfile mua = muaProfileRepository.findById(muaId)
                .orElseThrow(() -> new RuntimeException("MUA not found"));
        mua.setVerified(true);
        muaProfileRepository.save(mua);
    }

    @Transactional
    public void featureMua(UUID muaId, boolean featured) {
        MuaProfile mua = muaProfileRepository.findById(muaId)
                .orElseThrow(() -> new RuntimeException("MUA not found"));
        mua.setFeatured(featured);
        muaProfileRepository.save(mua);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        muaProfileRepository.findByUserId(userId).ifPresent(m -> {
            m.setActive(false);
            muaProfileRepository.save(m);
        });
    }

    public List<AdminMuaDto> listAllMuas() {
        return muaProfileRepository.findAll().stream().map(m -> AdminMuaDto.builder()
                .id(m.getId().toString())
                .displayName(m.getDisplayName())
                .email(m.getUser().getEmail())
                .city(m.getCity())
                .onboardingComplete(Boolean.TRUE.equals(m.getOnboardingComplete()))
                .build()).collect(Collectors.toList());
    }

    public static void requireAdmin(String role) {
        if (!UserRole.ADMIN.name().equals(role)) {
            throw new RuntimeException("Admin access required");
        }
    }
}
