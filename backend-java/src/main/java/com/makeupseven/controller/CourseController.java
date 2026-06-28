package com.makeupseven.controller;

import com.makeupseven.dto.CourseResponse;
import com.makeupseven.dto.EnrollResponse;
import com.makeupseven.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public List<CourseResponse> list() {
        return courseService.listPublished();
    }

    @GetMapping("/my/enrollments")
    public List<UUID> myEnrollments(Authentication auth) {
        return courseService.getEnrolledCourseIds(UUID.fromString(auth.getName()));
    }

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable UUID id) {
        return courseService.getById(id);
    }

    @PostMapping("/{id}/enroll")
    public EnrollResponse enroll(@PathVariable UUID id, Authentication auth) {
        return courseService.enroll(UUID.fromString(auth.getName()), id);
    }

    @PostMapping("/payments/verify")
    public EnrollResponse verifyPayment(@RequestBody java.util.Map<String, String> body) {
        return courseService.verifyEnrollmentPayment(
                body.get("orderId"),
                body.get("paymentId"),
                body.get("signature"));
    }
}
