package com.makeupseven.service;

import com.makeupseven.dto.ContractResponse;
import com.makeupseven.model.Booking;
import com.makeupseven.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final BookingRepository bookingRepository;

    @Value("${makeupseven.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public ContractResponse getContract(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getClient().getId().equals(userId)
                && !booking.getMuaProfile().getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return toResponse(booking);
    }

    @Transactional
    public ContractResponse signContract(UUID bookingId, UUID clientId, String signatureName) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getClient().getId().equals(clientId)) {
            throw new RuntimeException("Only the client can sign the contract");
        }
        booking.setContractSignedAt(Instant.now().toString() + "|" + signatureName);
        if (booking.getContractUrl() == null) {
            booking.setContractUrl(frontendUrl + "/contracts/" + bookingId);
        }
        bookingRepository.save(booking);
        return toResponse(booking);
    }

    public String contractUrlFor(Booking booking) {
        return frontendUrl + "/contracts/" + booking.getId();
    }

    private ContractResponse toResponse(Booking b) {
        BigDecimal remaining = b.getTotalAmount().subtract(
                b.getDepositAmount() != null ? b.getDepositAmount() : BigDecimal.ZERO);
        return ContractResponse.builder()
                .bookingId(b.getId())
                .clientName(b.getClient().getFullName())
                .muaName(b.getMuaProfile().getDisplayName())
                .serviceName(b.getService() != null ? b.getService().getName() : "Makeup Session")
                .bookingDate(b.getBookingDate())
                .startTime(b.getStartTime())
                .totalAmount(b.getTotalAmount())
                .depositAmount(b.getDepositAmount())
                .remainingAmount(remaining)
                .terms(buildTerms(b))
                .contractSignedAt(b.getContractSignedAt())
                .signed(b.getContractSignedAt() != null)
                .build();
    }

    private String buildTerms(Booking b) {
        return String.format(
            "MakeupSeven Service Agreement\n\n" +
            "Artist: %s\nClient: %s\nDate: %s at %s\n" +
            "Total: ₹%s | Deposit paid: ₹%s | Balance due on day of service: ₹%s\n\n" +
            "1. Client agrees to pay remaining balance on service date.\n" +
            "2. Cancellation within 48hrs forfeits deposit.\n" +
            "3. Artist provides professional makeup services as agreed.\n" +
            "4. MakeupSeven platform fee (10%%) is included in pricing.\n" +
            "5. Disputes resolved via MakeupSeven support within 7 days.",
            b.getMuaProfile().getDisplayName(),
            b.getClient().getFullName(),
            b.getBookingDate(),
            b.getStartTime(),
            b.getTotalAmount(),
            b.getDepositAmount(),
            b.getTotalAmount().subtract(b.getDepositAmount())
        );
    }
}
