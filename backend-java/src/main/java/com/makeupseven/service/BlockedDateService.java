package com.makeupseven.service;

import com.makeupseven.dto.BlockedDateDto;
import com.makeupseven.model.BlockedDate;
import com.makeupseven.model.MuaProfile;
import com.makeupseven.repository.BlockedDateRepository;
import com.makeupseven.repository.MuaProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockedDateService {

    private final BlockedDateRepository blockedDateRepository;
    private final MuaProfileRepository muaProfileRepository;

    public List<BlockedDateDto> getBlockedDates(UUID userId) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        return blockedDateRepository.findByMuaProfileIdOrderByBlockDateAsc(mua.getId()).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public BlockedDateDto blockDate(UUID userId, BlockedDateDto dto) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        if (blockedDateRepository.existsByMuaProfileIdAndBlockDate(mua.getId(), dto.getBlockDate())) {
            throw new RuntimeException("Date already blocked");
        }
        BlockedDate blocked = BlockedDate.builder()
                .muaProfile(mua)
                .blockDate(dto.getBlockDate())
                .reason(dto.getReason())
                .build();
        return toDto(blockedDateRepository.save(blocked));
    }

    @Transactional
    public void unblockDate(UUID userId, LocalDate date) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        blockedDateRepository.deleteByMuaProfileIdAndBlockDate(mua.getId(), date);
    }

    public boolean isDateBlocked(UUID muaId, LocalDate date) {
        return blockedDateRepository.existsByMuaProfileIdAndBlockDate(muaId, date);
    }

    private BlockedDateDto toDto(BlockedDate b) {
        return BlockedDateDto.builder()
                .id(b.getId())
                .blockDate(b.getBlockDate())
                .reason(b.getReason())
                .build();
    }
}
