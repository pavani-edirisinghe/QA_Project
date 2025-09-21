package com.brightpath.backend.service;

import com.brightpath.backend.model.Course;
import com.brightpath.backend.repository.CourseRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private List<Course> testCourses;

    @BeforeEach
    void setUp() {
        // Create test course with ID
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Java Programming");
        testCourse.setDescription("Learn Java basics");
        testCourse.setPrice(299.99);

        try {
            testCourse.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-15"));
        } catch (ParseException e) {
            testCourse.setStartDate(new Date());
        }

        testCourse.setImageUrl("/courses/java-image.jpg");

        // Create test course list
        testCourses = Arrays.asList(
                testCourse,
                createCourse(2L, "Python Fundamentals", "Learn Python", 199.99, "2025-02-01"),
                createCourse(3L, "Web Development", "HTML/CSS/JS", 399.99, "2025-03-01")
        );
    }

    /**
     * UNIT TEST 1: Save Course (Create New Course)
     * Tests the "Save Course – Admin adds a new course" TDD scenario
     */
    @Test
    void testSaveCourse_NewCourse() {
        System.out.println("UNIT TEST 1: Save New Course");

        // Arrange - Create new course without ID (should be generated)
        Course newCourse = new Course();
        newCourse.setName("New React Course");
        newCourse.setDescription("Learn React.js framework");
        newCourse.setPrice(249.99);
        newCourse.setStartDate(new Date());
        // Note: ID is null - repository should generate it

        // FIXED: Mock repository to return course with generated ID
        Course savedCourseWithId = new Course();
        savedCourseWithId.setId(100L); // Mocked generated ID
        savedCourseWithId.setName("New React Course");
        savedCourseWithId.setDescription("Learn React.js framework");
        savedCourseWithId.setPrice(249.99);
        savedCourseWithId.setStartDate(newCourse.getStartDate());

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourseWithId);

        // Act - Call service method
        Course savedCourse = courseService.saveCourse(newCourse);

        // Assert - Verify results
        assertAll("Save New Course Validation",
                () -> assertNotNull(savedCourse, "Saved course should not be null"),
                () -> assertNotNull(savedCourse.getId(), "Course should have an ID"), // FIXED
                () -> assertTrue(savedCourse.getId() > 0, "ID should be positive"), // FIXED
                () -> assertEquals("New React Course", savedCourse.getName(), "Name should match"),
                () -> assertEquals("Learn React.js framework", savedCourse.getDescription(), "Description should match"),
                () -> assertEquals(249.99, savedCourse.getPrice(), 0.01, "Price should match"),

                // Verify repository interaction
                () -> verify(courseRepository, times(1)).save(any(Course.class)),
                () -> verifyNoMoreInteractions(courseRepository)
        );

        System.out.println("Course saved successfully with ID: " + savedCourse.getId());
    }

    /**
     * UNIT TEST 2: Update Course (Update Existing Course)
     * Tests the "Update Course – Update an existing course" TDD scenario
     */
    @Test
    void testUpdateCourse_ExistingCourse() {
        System.out.println("UNIT TEST 2: Update Existing Course ");

        // Arrange - Setup mock behavior
        Course updateData = new Course();
        updateData.setId(1L);
        updateData.setName("Java Programming - Updated");
        updateData.setDescription("Updated Java course with advanced topics");
        updateData.setPrice(349.99);
        updateData.setStartDate(new Date());
        updateData.setImageUrl("/courses/java-updated.jpg");

        // Mock repository to return existing course first
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // FIXED: Mock save to return updated course with same ID
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(1L); // Preserve existing ID
            return course;
        });

        // Act - Call service method
        Course result = courseService.updateCourse(1L, updateData);

        // Assert - Verify results
        assertAll("Update Course Validation",
                () -> assertNotNull(result, "Updated course should not be null"),
                () -> assertEquals(1L, result.getId(), "ID should remain the same"),
                () -> assertEquals("Java Programming - Updated", result.getName(), "Name should be updated"),
                () -> assertEquals("Updated Java course with advanced topics", result.getDescription(), "Description should be updated"),
                () -> assertEquals(349.99, result.getPrice(), 0.01, "Price should be updated"),
                () -> assertEquals("/courses/java-updated.jpg", result.getImageUrl(), "Image URL should be updated"),

                // Verify repository interactions
                () -> verify(courseRepository, times(1)).findById(1L),
                () -> verify(courseRepository, times(1)).save(any(Course.class)),
                () -> verifyNoMoreInteractions(courseRepository)
        );

        System.out.println("Course updated successfully");
    }

    @Test
    void testGetAllCourses_EmptyList() {
        System.out.println("UNIT TEST 3: Get All Courses - Empty List");

        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        List<Course> result = courseService.getAllCourses();

        assertAll("Get All Courses Empty",
                () -> assertNotNull(result, "Should not return null"),
                () -> assertTrue(result.isEmpty(), "Should return empty list"),
                () -> assertEquals(0, result.size(), "Size should be 0"),
                () -> verify(courseRepository, times(1)).findAll()
        );

        System.out.println("Empty courses list test passed");
    }

    @Test
    void testGetAllCourses_MultipleCourses() {
        System.out.println("UNIT TEST 4: Get All Courses - Multiple Courses");

        when(courseRepository.findAll()).thenReturn(testCourses);

        List<Course> result = courseService.getAllCourses();

        assertAll("Get All Courses Multiple",
                () -> assertNotNull(result, "Should not return null"),
                () -> assertEquals(3, result.size(), "Should return 3 courses"),
                () -> assertEquals("Java Programming", result.get(0).getName()),
                () -> assertEquals(1L, result.get(0).getId(), "First course ID should be 1"),
                () -> verify(courseRepository, times(1)).findAll()
        );

        System.out.println("Multiple courses test passed");
    }

    // Helper method to create test courses
    private Course createCourse(Long id, String name, String description, double price, String startDateStr) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDescription(description);
        course.setPrice(price);

        try {
            course.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr));
        } catch (ParseException e) {
            course.setStartDate(new Date());
        }

        return course;
    }
}