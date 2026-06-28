package com.makeupseven.service;

import com.makeupseven.model.Booking;
import com.makeupseven.model.QuoteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    @Value("${makeupseven.python-service-url:http://localhost:8000}")
    private String pythonServiceUrl;

    @Value("${makeupseven.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private final RestClient restClient = RestClient.create();

    public void sendBookingConfirmation(Booking booking) {
        post("/api/notifications/booking-confirmation", Map.of(
                "booking_id", booking.getId().toString(),
                "client_name", booking.getClient().getFullName(),
                "client_phone", booking.getClient().getPhone() != null ? booking.getClient().getPhone() : "",
                "client_email", booking.getClient().getEmail(),
                "mua_name", booking.getMuaProfile().getDisplayName(),
                "mua_phone", booking.getMuaProfile().getUser().getPhone() != null ? booking.getMuaProfile().getUser().getPhone() : "",
                "date", booking.getBookingDate().toString(),
                "time", booking.getStartTime().toString(),
                "amount", booking.getTotalAmount().toString()
        ));
    }

    public void sendReviewRequest(Booking booking) {
        post("/api/notifications/review-request", Map.of(
                "booking_id", booking.getId().toString(),
                "client_name", booking.getClient().getFullName(),
                "client_phone", booking.getClient().getPhone() != null ? booking.getClient().getPhone() : "",
                "client_email", booking.getClient().getEmail(),
                "mua_name", booking.getMuaProfile().getDisplayName(),
                "frontend_url", frontendUrl
        ));
    }

    public void sendAppointmentReminder(Booking booking) {
        post("/api/notifications/appointment-reminder", Map.of(
                "booking_id", booking.getId().toString(),
                "client_name", booking.getClient().getFullName(),
                "client_phone", booking.getClient().getPhone() != null ? booking.getClient().getPhone() : "",
                "client_email", booking.getClient().getEmail(),
                "mua_name", booking.getMuaProfile().getDisplayName(),
                "date", booking.getBookingDate().toString(),
                "time", booking.getStartTime().toString()
        ));
    }

    public void sendQuoteRequest(QuoteRequest quote) {
        post("/api/notifications/quote-request", Map.of(
                "quote_id", quote.getId().toString(),
                "client_name", quote.getClient().getFullName(),
                "mua_phone", quote.getMuaProfile().getUser().getPhone() != null ? quote.getMuaProfile().getUser().getPhone() : "",
                "mua_name", quote.getMuaProfile().getDisplayName(),
                "details", quote.getDetails() != null ? quote.getDetails() : ""
        ));
    }

    public void sendQuoteResponse(QuoteRequest quote) {
        post("/api/notifications/quote-response", Map.of(
                "quote_id", quote.getId().toString(),
                "client_phone", quote.getClient().getPhone() != null ? quote.getClient().getPhone() : "",
                "client_email", quote.getClient().getEmail(),
                "mua_name", quote.getMuaProfile().getDisplayName(),
                "quoted_amount", quote.getQuotedAmount() != null ? quote.getQuotedAmount().toString() : "0"
        ));
    }

    public void sendOtp(String phone, String code) {
        post("/api/notifications/otp", Map.of(
                "phone", phone != null ? phone : "",
                "code", code
        ));
    }

    private void post(String path, Map<String, String> body) {
        try {
            restClient.post()
                    .uri(pythonServiceUrl + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notification failed {}: {}", path, e.getMessage());
        }
    }
}
