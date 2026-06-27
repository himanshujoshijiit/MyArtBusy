package com.makeupseven.controller;

import com.makeupseven.dto.ContractResponse;
import com.makeupseven.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/{bookingId}")
    public ContractResponse getContract(@PathVariable UUID bookingId, Authentication auth) {
        return contractService.getContract(bookingId, UUID.fromString(auth.getName()));
    }

    @PostMapping("/{bookingId}/sign")
    public ContractResponse sign(@PathVariable UUID bookingId, Authentication auth,
                                 @RequestBody Map<String, String> body) {
        return contractService.signContract(bookingId, UUID.fromString(auth.getName()),
                body.getOrDefault("signatureName", "Client"));
    }
}
