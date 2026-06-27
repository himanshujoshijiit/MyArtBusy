package com.makeupseven.service;

import com.makeupseven.dto.CourseResponse;
import com.makeupseven.model.Course;
import com.makeupseven.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseResponse> listPublished() {
        return courseRepository.findByPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public CourseResponse getById(UUID id) {
        Course c = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
        return toResponse(c);
    }

    @Transactional
    public CourseResponse enroll(UUID courseId) {
        Course c = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        c.setEnrollmentCount(c.getEnrollmentCount() + 1);
        return toResponse(courseRepository.save(c));
    }

    private CourseResponse toResponse(Course c) {
        return CourseResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .instructorName(c.getInstructorName())
                .thumbnailUrl(c.getThumbnailUrl())
                .price(c.getPrice())
                .durationHours(c.getDurationHours())
                .level(c.getLevel())
                .enrollmentCount(c.getEnrollmentCount())
                .build();
    }
}
