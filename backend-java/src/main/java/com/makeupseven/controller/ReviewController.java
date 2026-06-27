package com.makeupseven.controller;

import com.makeupseven.dto.CreateReviewRequest;
import com.makeupseven.dto.ReviewResponse;
import com.makeupseven.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewResponse create(Authentication auth, @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(UUID.fromString(auth.getName()), request);
    }

    @GetMapping("/mua/{muaId}")
    public List<ReviewResponse> getMuaReviews(@PathVariable UUID muaId) {
        return reviewService.getMuaReviews(muaId);
    }
}
