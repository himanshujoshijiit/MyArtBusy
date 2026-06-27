package com.makeupseven.controller;

import com.makeupseven.dto.BlockedDateDto;
import com.makeupseven.service.BlockedDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blocked-dates")
@RequiredArgsConstructor
public class BlockedDateController {

    private final BlockedDateService blockedDateService;

    @GetMapping
    public List<BlockedDateDto> list(Authentication auth) {
        return blockedDateService.getBlockedDates(UUID.fromString(auth.getName()));
    }

    @PostMapping
    public BlockedDateDto block(Authentication auth, @RequestBody BlockedDateDto dto) {
        return blockedDateService.blockDate(UUID.fromString(auth.getName()), dto);
    }

    @DeleteMapping("/{date}")
    public void unblock(Authentication auth,
                        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        blockedDateService.unblockDate(UUID.fromString(auth.getName()), date);
    }
}
