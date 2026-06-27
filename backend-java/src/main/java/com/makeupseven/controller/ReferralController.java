package com.makeupseven.controller;

import com.makeupseven.dto.ReferralDto;
import com.makeupseven.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping("/my")
    public ReferralDto myReferral(Authentication auth) {
        return referralService.getOrCreateReferralCode(UUID.fromString(auth.getName()));
    }

    @PostMapping("/apply")
    public ReferralDto apply(Authentication auth, @RequestBody Map<String, String> body) {
        return referralService.applyReferralCode(UUID.fromString(auth.getName()), body.get("code"));
    }
}
