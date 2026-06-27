package com.makeupseven.service;

import com.makeupseven.dto.*;
import com.makeupseven.model.*;
import com.makeupseven.model.enums.*;
import com.makeupseven.repository.*;
import com.makeupseven.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MuaProfileRepository muaProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

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
            MuaProfile profile = MuaProfile.builder()
                    .user(user)
                    .displayName(request.getFullName())
                    .city("Bengaluru")
                    .country("India")
                    .build();
            profile = muaProfileRepository.save(profile);
            muaProfileId = profile.getId();
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .muaProfileId(muaProfileId)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        UUID muaProfileId = muaProfileRepository.findByUserId(user.getId())
                .map(MuaProfile::getId).orElse(null);
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .muaProfileId(muaProfileId)
                .build();
    }
}
