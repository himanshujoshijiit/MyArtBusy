package com.makeupseven.controller;

import com.makeupseven.dto.*;
import com.makeupseven.service.BookingService;
import com.makeupseven.service.MuaProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final MuaProfileService muaProfileService;

    @PostMapping
    public BookingResponse create(Authentication auth, @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(UUID.fromString(auth.getName()), request);
    }

    @GetMapping("/my")
    public List<BookingResponse> myBookings(Authentication auth) {
        return bookingService.getClientBookings(UUID.fromString(auth.getName()));
    }

    @GetMapping("/mua")
    public List<BookingResponse> muaBookings(Authentication auth) {
        return bookingService.getMuaBookings(
                muaProfileService.getByUserId(UUID.fromString(auth.getName())).getId());
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable UUID id, Authentication auth) {
        return bookingService.getBooking(id, UUID.fromString(auth.getName()));
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(Authentication auth, @PathVariable UUID id) {
        return bookingService.cancelBooking(id, UUID.fromString(auth.getName()));
    }

    @PostMapping("/{id}/confirm")
    public BookingResponse confirm(Authentication auth, @PathVariable UUID id) {
        return bookingService.confirmBooking(id, UUID.fromString(auth.getName()));
    }

    @PostMapping("/{id}/complete")
    public BookingResponse complete(Authentication auth, @PathVariable UUID id) {
        return bookingService.completeBooking(id, UUID.fromString(auth.getName()));
    }

    @GetMapping("/availability/{muaId}")
    public List<AvailabilitySlotDto> availability(
            @PathVariable UUID muaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return bookingService.getAvailability(muaId, start, end);
    }

    @PostMapping("/availability")
    public AvailabilitySlotDto addAvailability(Authentication auth, @RequestBody AvailabilitySlotDto dto) {
        return bookingService.addAvailability(UUID.fromString(auth.getName()), dto);
    }
}
