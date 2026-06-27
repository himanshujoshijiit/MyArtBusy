package com.makeupseven.service;

import com.makeupseven.model.Booking;
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

    private final RestClient restClient = RestClient.create();

    public void sendBookingConfirmation(Booking booking) {
        try {
            restClient.post()
                    .uri(pythonServiceUrl + "/api/notifications/booking-confirmation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "booking_id", booking.getId().toString(),
                            "client_name", booking.getClient().getFullName(),
                            "client_phone", booking.getClient().getPhone(),
                            "mua_name", booking.getMuaProfile().getDisplayName(),
                            "mua_phone", booking.getMuaProfile().getUser().getPhone(),
                            "date", booking.getBookingDate().toString(),
                            "time", booking.getStartTime().toString(),
                            "amount", booking.getTotalAmount().toString()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send booking notification: {}", e.getMessage());
        }
    }

    public void sendReviewRequest(Booking booking) {
        try {
            restClient.post()
                    .uri(pythonServiceUrl + "/api/notifications/review-request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "booking_id", booking.getId().toString(),
                            "client_name", booking.getClient().getFullName(),
                            "client_phone", booking.getClient().getPhone(),
                            "mua_name", booking.getMuaProfile().getDisplayName()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send review request: {}", e.getMessage());
        }
    }
}
