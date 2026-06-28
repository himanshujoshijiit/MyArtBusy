package com.makeupseven.controller;

import com.makeupseven.dto.*;
import com.makeupseven.service.MuaProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/muas")
@RequiredArgsConstructor
public class MuaController {

    private final MuaProfileService muaProfileService;

    @GetMapping
    public List<MuaProfileResponse> getAll() {
        return muaProfileService.getAll();
    }

    @GetMapping("/top")
    public List<MuaProfileResponse> getTopArtists() {
        return muaProfileService.getTopArtists();
    }

    @GetMapping("/city/{city}")
    public List<MuaProfileResponse> getByCity(@PathVariable String city) {
        return muaProfileService.getByCity(city);
    }

    @GetMapping("/primary")
    public MuaProfileResponse getPrimary() {
        return muaProfileService.getPrimaryArtist();
    }

    @GetMapping("/{id}")
    public MuaProfileResponse getById(@PathVariable UUID id) {
        return muaProfileService.getById(id);
    }

    @GetMapping("/profile/me")
    public MuaProfileResponse getMyProfile(Authentication auth) {
        return muaProfileService.getProfileByUserId(UUID.fromString(auth.getName()));
    }

    @PutMapping("/profile")
    public MuaProfileResponse updateProfile(Authentication auth, @Valid @RequestBody MuaProfileRequest request) {
        return muaProfileService.updateProfile(UUID.fromString(auth.getName()), request);
    }

    @PostMapping("/portfolio")
    public PortfolioItemDto addPortfolio(Authentication auth, @RequestBody PortfolioItemDto dto) {
        return muaProfileService.addPortfolioItem(UUID.fromString(auth.getName()), dto);
    }

    @PostMapping("/services")
    public MuaServiceDto addService(Authentication auth, @RequestBody MuaServiceDto dto) {
        return muaProfileService.addService(UUID.fromString(auth.getName()), dto);
    }
}
