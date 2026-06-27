package com.makeupseven.controller;

import com.makeupseven.dto.CourseResponse;
import com.makeupseven.service.CourseService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable UUID id) {
        return courseService.getById(id);
    }

    @PostMapping("/{id}/enroll")
    public CourseResponse enroll(@PathVariable UUID id) {
        return courseService.enroll(id);
    }
}
