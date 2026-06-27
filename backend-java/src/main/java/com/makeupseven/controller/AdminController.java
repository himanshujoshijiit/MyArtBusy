package com.makeupseven.controller;

import com.makeupseven.dto.*;
import com.makeupseven.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public AdminStatsDto stats() {
        return adminService.getStats();
    }

    @GetMapping("/muas")
    public List<AdminMuaDto> listMuas() {
        return adminService.listAllMuas();
    }

    @PostMapping("/muas/{id}/verify")
    public void verifyMua(@PathVariable UUID id) {
        adminService.verifyMua(id);
    }

    @PostMapping("/muas/{id}/feature")
    public void featureMua(@PathVariable UUID id, @RequestParam boolean featured) {
        adminService.featureMua(id, featured);
    }

    @PostMapping("/users/{id}/deactivate")
    public void deactivate(@PathVariable UUID id) {
        adminService.deactivateUser(id);
    }
}
