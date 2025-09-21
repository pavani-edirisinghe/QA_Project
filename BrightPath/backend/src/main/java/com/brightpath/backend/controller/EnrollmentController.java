package com.brightpath.backend.controller;

import com.brightpath.backend.model.Enrollment;
import com.brightpath.backend.model.User;
import com.brightpath.backend.model.Course;
import com.brightpath.backend.service.EnrollmentService;
import com.brightpath.backend.service.UserService;
import com.brightpath.backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "http://localhost:5173")
public class EnrollmentController {
    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @PostMapping("/{userId}/{courseId}")
    public ResponseEntity<?> enrollInCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {

        User user = userService.findById(userId);
        Course course = courseService.getCourseById(courseId);

        // Check if user is already enrolled
        if (enrollmentService.isUserEnrolled(userId, courseId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("User is already enrolled in this course");
        }

        Enrollment enrollment = enrollmentService.enrollUser(user, course);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Enrollment>> getUserEnrollments(
            @PathVariable Long userId) {

        List<Enrollment> enrollments = enrollmentService.getUserEnrollments(userId);
        return ResponseEntity.ok(enrollments);
    }
}