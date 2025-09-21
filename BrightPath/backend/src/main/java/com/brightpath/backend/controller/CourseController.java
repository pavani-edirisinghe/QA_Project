package com.brightpath.backend.controller;

import com.brightpath.backend.model.Course;
import com.brightpath.backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:5173")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // Inject the course upload directory from application.properties
    @Value("${course.upload-dir}")
    private String courseUploadDir;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Course> createCourse(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("price") Double price,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        System.out.println("📩 Received course creation request:");
        System.out.println("name: " + name);
        System.out.println("description: " + description);
        System.out.println("startDateStr: " + startDateStr);
        System.out.println("price: " + price);

        if (imageFile != null) {
            System.out.println("image file: " + imageFile.getOriginalFilename());
        } else {
            System.out.println("image file: null");
        }

        String imageUrl = null;

        try {
            // Use the injected course upload directory
            File uploadDir = new File(courseUploadDir);

            // Create directory if it doesn't exist
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("Created upload dir: " + created + " at " + uploadDir.getAbsolutePath());
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                File dest = new File(uploadDir, fileName);

                System.out.println("Saving file to: " + dest.getAbsolutePath());
                imageFile.transferTo(dest);

                // Set URL to match resource handler pattern
                imageUrl = "/courses/" + fileName;
                System.out.println("Image saved: " + imageUrl);
            }

            // Parse date
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr);

            // Create course object
            Course course = new Course();
            course.setName(name);
            course.setDescription(description);
            course.setStartDate(startDate);
            course.setPrice(price);
            course.setImageUrl(imageUrl);

            // Save course
            Course savedCourse = courseService.saveCourse(course);
            System.out.println("Course saved to DB with ID: " + savedCourse.getId());

            return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course courseDetails) {
        Course updatedCourse = courseService.updateCourse(id, courseDetails);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
}