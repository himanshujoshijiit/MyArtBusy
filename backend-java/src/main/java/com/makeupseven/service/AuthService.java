package com.makeupseven.service;

import com.makeupseven.dto.*;
import com.makeupseven.model.*;
import com.makeupseven.model.enums.*;
import com.makeupseven.repository.*;
import com.makeupseven.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MuaProfileRepository muaProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final MuaProfileService muaProfileService;
    private final NotificationClient notificationClient;

    @Value("${makeupseven.default-mua-tier:FREE}")
    private String defaultMuaTier;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();
        user = userRepository.save(user);

        UUID muaProfileId = null;
        if (request.getRole() == UserRole.MUA) {
            SubscriptionTier tier = SubscriptionTier.FREE;
            try {
                tier = SubscriptionTier.valueOf(defaultMuaTier.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // keep FREE
            }
            MuaProfile profile = MuaProfile.builder()
                    .user(user)
                    .displayName(request.getFullName())
                    .city("Bengaluru")
                    .country("India")
                    .onboardingComplete(false)
                    .subscriptionTier(tier)
                    .build();
            profile = muaProfileRepository.save(profile);
            muaProfileId = profile.getId();
        }

        return buildAuthResponse(user, muaProfileId);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        UUID muaProfileId = muaProfileRepository.findByUserId(user.getId())
                .map(MuaProfile::getId).orElse(null);
        return buildAuthResponse(user, muaProfileId);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        User user = stored.getUser();
        UUID muaProfileId = muaProfileRepository.findByUserId(user.getId())
                .map(MuaProfile::getId).orElse(null);
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return buildAuthResponse(user, muaProfileId);
    }

    @Transactional
    public void sendOtp(OtpRequest request) {
        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        otpCodeRepository.save(OtpCode.builder()
                .phone(request.getPhone())
                .code(code)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build());
        notificationClient.sendOtp(request.getPhone(), code);
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        OtpCode otp = otpCodeRepository.findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(request.getPhone())
                .orElseThrow(() -> new RuntimeException("No OTP found"));
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }
        if (!otp.getCode().equals(request.getCode())) {
            throw new RuntimeException("Invalid OTP");
        }
        otp.setUsed(true);
        otpCodeRepository.save(otp);

        User user = userRepository.findByPhone(request.getPhone()).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(request.getPhone() + "@phone.makeupseven.com")
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .fullName(request.getFullName() != null ? request.getFullName() : "Client")
                        .phone(request.getPhone())
                        .role(UserRole.CLIENT)
                        .build()));

        UUID muaProfileId = muaProfileRepository.findByUserId(user.getId())
                .map(MuaProfile::getId).orElse(null);
        return buildAuthResponse(user, muaProfileId);
    }

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(request.getEmail())
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .fullName(request.getFullName())
                        .role(UserRole.CLIENT)
                        .build()));
        UUID muaProfileId = muaProfileRepository.findByUserId(user.getId())
                .map(MuaProfile::getId).orElse(null);
        return buildAuthResponse(user, muaProfileId);
    }

    @Transactional
    public MuaProfileResponse completeOnboarding(UUID userId, MuaProfileRequest request) {
        MuaProfile profile = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        profile.setDisplayName(request.getDisplayName());
        profile.setBio(request.getBio());
        profile.setCity(request.getCity());
        profile.setLocality(request.getLocality());
        if (request.getPincode() != null) profile.setPincode(request.getPincode());
        if (request.getLatitude() != null) profile.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) profile.setLongitude(request.getLongitude());
        if (request.getOccasions() != null) {
            profile.getOccasions().clear();
            profile.getOccasions().addAll(request.getOccasions());
        }
        if (request.getSkinToneExpertise() != null) {
            profile.getSkinToneExpertise().clear();
            profile.getSkinToneExpertise().addAll(request.getSkinToneExpertise());
        }
        if (request.getMinPrice() != null) profile.setMinPrice(request.getMinPrice());
        if (request.getMaxPrice() != null) profile.setMaxPrice(request.getMaxPrice());
        profile.setOnboardingComplete(true);
        muaProfileRepository.save(profile);
        return muaProfileService.getProfileByUserId(userId);
    }

    private AuthResponse buildAuthResponse(User user, UUID muaProfileId) {
        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenValue = tokenProvider.generateRefreshToken(user.getId());
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build());
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshTokenValue)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .muaProfileId(muaProfileId)
                .build();
    }
}
