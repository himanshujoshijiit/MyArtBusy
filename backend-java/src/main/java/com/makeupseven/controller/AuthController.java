package com.makeupseven.controller;

import com.makeupseven.dto.*;
import com.makeupseven.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }

    @PostMapping("/otp/send")
    public MapResponse sendOtp(@RequestBody OtpRequest request) {
        authService.sendOtp(request);
        return new MapResponse("OTP sent");
    }

    @PostMapping("/otp/verify")
    public AuthResponse verifyOtp(@RequestBody OtpVerifyRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/google")
    public AuthResponse googleAuth(@RequestBody GoogleAuthRequest request) {
        return authService.googleAuth(request);
    }

    @PostMapping("/onboarding")
    public MuaProfileResponse completeOnboarding(Authentication auth,
                                                   @Valid @RequestBody MuaProfileRequest request) {
        return authService.completeOnboarding(
                java.util.UUID.fromString(auth.getName()), request);
    }

    record MapResponse(String message) {}
}
