package com.makeupseven.controller;

import com.makeupseven.dto.*;
import com.makeupseven.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsResponse stats(Authentication auth) {
        return dashboardService.getStats(UUID.fromString(auth.getName()));
    }

    @GetMapping("/clients")
    public List<ClientFaceProfileResponse> clientProfiles(Authentication auth) {
        return dashboardService.getClientProfiles(UUID.fromString(auth.getName()));
    }

    @PostMapping("/clients")
    public ClientFaceProfileResponse saveClientProfile(Authentication auth, @RequestBody ClientFaceProfileRequest request) {
        return dashboardService.saveClientProfile(UUID.fromString(auth.getName()), request);
    }

    @GetMapping("/kit")
    public List<KitItemResponse> kitItems(Authentication auth) {
        return dashboardService.getKitItems(UUID.fromString(auth.getName()));
    }

    @PostMapping("/kit")
    public KitItemResponse addKitItem(Authentication auth, @RequestBody KitItemRequest request) {
        return dashboardService.addKitItem(UUID.fromString(auth.getName()), request);
    }
}
