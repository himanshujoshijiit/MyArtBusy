package com.makeupseven.service;

import com.makeupseven.dto.*;
import com.makeupseven.model.*;
import com.makeupseven.model.enums.QuoteStatus;
import com.makeupseven.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRequestRepository quoteRepository;
    private final UserRepository userRepository;
    private final MuaProfileRepository muaProfileRepository;
    private final NotificationClient notificationClient;

    @Transactional
    public QuoteRequestDto createQuote(UUID clientId, CreateQuoteRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        MuaProfile mua = muaProfileRepository.findById(request.getMuaId())
                .orElseThrow(() -> new RuntimeException("MUA not found"));

        QuoteRequest quote = QuoteRequest.builder()
                .client(client)
                .muaProfile(mua)
                .occasion(request.getOccasion())
                .eventDate(request.getEventDate())
                .details(request.getDetails())
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .status(QuoteStatus.PENDING)
                .build();
        quote = quoteRepository.save(quote);
        notificationClient.sendQuoteRequest(quote);
        return toDto(quote);
    }

    public List<QuoteRequestDto> getClientQuotes(UUID clientId) {
        return quoteRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public List<QuoteRequestDto> getMuaQuotes(UUID userId) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        return quoteRepository.findByMuaProfileIdOrderByCreatedAtDesc(mua.getId()).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public QuoteRequestDto respondToQuote(UUID userId, UUID quoteId, QuoteResponseRequest request) {
        MuaProfile mua = muaProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("MUA profile not found"));
        QuoteRequest quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        if (!quote.getMuaProfile().getId().equals(mua.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        quote.setQuotedAmount(request.getQuotedAmount());
        quote.setMuaResponse(request.getMuaResponse());
        if (request.getStatus() != null) {
            quote.setStatus(QuoteStatus.valueOf(request.getStatus()));
        } else {
            quote.setStatus(QuoteStatus.QUOTED);
        }
        quote = quoteRepository.save(quote);
        notificationClient.sendQuoteResponse(quote);
        return toDto(quote);
    }

    private QuoteRequestDto toDto(QuoteRequest q) {
        return QuoteRequestDto.builder()
                .id(q.getId())
                .clientId(q.getClient().getId())
                .clientName(q.getClient().getFullName())
                .muaId(q.getMuaProfile().getId())
                .muaName(q.getMuaProfile().getDisplayName())
                .occasion(q.getOccasion())
                .eventDate(q.getEventDate())
                .details(q.getDetails())
                .budgetMin(q.getBudgetMin())
                .budgetMax(q.getBudgetMax())
                .status(q.getStatus())
                .quotedAmount(q.getQuotedAmount())
                .muaResponse(q.getMuaResponse())
                .createdAt(q.getCreatedAt())
                .build();
    }
}
