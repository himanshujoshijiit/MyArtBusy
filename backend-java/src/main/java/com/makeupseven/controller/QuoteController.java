package com.makeupseven.controller;

import com.makeupseven.dto.*;
import com.makeupseven.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public QuoteRequestDto create(Authentication auth, @Valid @RequestBody CreateQuoteRequest request) {
        return quoteService.createQuote(UUID.fromString(auth.getName()), request);
    }

    @GetMapping("/my")
    public List<QuoteRequestDto> myQuotes(Authentication auth) {
        return quoteService.getClientQuotes(UUID.fromString(auth.getName()));
    }

    @GetMapping("/mua")
    public List<QuoteRequestDto> muaQuotes(Authentication auth) {
        return quoteService.getMuaQuotes(UUID.fromString(auth.getName()));
    }

    @PostMapping("/{id}/respond")
    public QuoteRequestDto respond(Authentication auth, @PathVariable UUID id,
                                   @RequestBody QuoteResponseRequest request) {
        return quoteService.respondToQuote(UUID.fromString(auth.getName()), id, request);
    }
}
