package com.makeupseven.service;

import com.makeupseven.dto.CreateReviewRequest;
import com.makeupseven.dto.ReviewResponse;
import com.makeupseven.model.*;
import com.makeupseven.model.enums.BookingStatus;
import com.makeupseven.repository.BookingRepository;
import com.makeupseven.repository.MuaProfileRepository;
import com.makeupseven.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final MuaProfileRepository muaProfileRepository;

    @Transactional
    public ReviewResponse createReview(UUID clientId, CreateReviewRequest request) {
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("Review already submitted for this booking");
        }
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getClient().getId().equals(clientId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Can only review completed bookings");
        }

        Review review = Review.builder()
                .booking(booking)
                .client(booking.getClient())
                .muaProfile(booking.getMuaProfile())
                .rating(request.getRating())
                .comment(request.getComment())
                .verified(true)
                .build();
        review = reviewRepository.save(review);

        updateMuaRating(booking.getMuaProfile().getId());
        return toResponse(review);
    }

    public List<ReviewResponse> getMuaReviews(UUID muaId) {
        return reviewRepository.findByMuaProfileIdOrderByCreatedAtDesc(muaId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private void updateMuaRating(UUID muaId) {
        List<Review> reviews = reviewRepository.findByMuaProfileIdOrderByCreatedAtDesc(muaId);
        MuaProfile mua = muaProfileRepository.findById(muaId).orElseThrow();
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        mua.setRating(Math.round(avg * 10.0) / 10.0);
        mua.setReviewCount(reviews.size());
        mua.setTopArtist(avg >= 4.5 && reviews.size() >= 5);
        muaProfileRepository.save(mua);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBooking().getId())
                .clientName(r.getClient().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .verified(r.getVerified())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
