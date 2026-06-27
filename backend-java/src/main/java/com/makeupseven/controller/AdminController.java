package com.makeupseven.controller;

import com.makeupseven.dto.*;
import com.makeupseven.service.AdminService;
import com.makeupseven.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtTokenProvider tokenProvider;

    private void checkAdmin(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        AdminService.requireAdmin(tokenProvider.getRoleFromToken(token));
    }

    @GetMapping("/stats")
    public AdminStatsDto stats(@RequestHeader("Authorization") String auth) {
        checkAdmin(auth);
        return adminService.getStats();
    }

    @GetMapping("/muas")
    public List<AdminMuaDto> listMuas(@RequestHeader("Authorization") String auth) {
        checkAdmin(auth);
        return adminService.listAllMuas();
    }

    @PostMapping("/muas/{id}/verify")
    public void verifyMua(@RequestHeader("Authorization") String auth, @PathVariable UUID id) {
        checkAdmin(auth);
        adminService.verifyMua(id);
    }

    @PostMapping("/muas/{id}/feature")
    public void featureMua(@RequestHeader("Authorization") String auth,
                           @PathVariable UUID id, @RequestParam boolean featured) {
        checkAdmin(auth);
        adminService.featureMua(id, featured);
    }

    @PostMapping("/users/{id}/deactivate")
    public void deactivate(@RequestHeader("Authorization") String auth, @PathVariable UUID id) {
        checkAdmin(auth);
        adminService.deactivateUser(id);
    }
}
