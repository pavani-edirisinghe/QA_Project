package com.brightpath.backend.repository;

import com.brightpath.backend.model.Course;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    private Course testCourse;

    @BeforeEach
    void setUp() throws ParseException {
        testCourse = new Course();
        testCourse.setName("Java Programming");
        testCourse.setDescription("Learn Java basics");
        testCourse.setPrice(299.99);
        testCourse.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-15"));

        entityManager.persistAndFlush(testCourse);
    }

    @Test
    void testSaveCourse() {
        System.out.println("🧪 REPO TEST 1: Save Course");

        Course savedCourse = courseRepository.save(testCourse);

        assertAll("Repository Save",
                () -> assertNotNull(savedCourse.getId()),
                () -> assertEquals("Java Programming", savedCourse.getName()),
                () -> assertTrue(courseRepository.findById(savedCourse.getId()).isPresent())
        );

        System.out.println("✅ Repository save test passed");
    }

    @Test
    void testFindCourseById() {
        System.out.println("🧪 REPO TEST 2: Find Course by ID");

        Optional<Course> foundCourse = courseRepository.findById(testCourse.getId());

        assertAll("Find by ID",
                () -> assertTrue(foundCourse.isPresent()),
                () -> assertEquals("Java Programming", foundCourse.get().getName()),
                () -> assertEquals(299.99, foundCourse.get().getPrice(), 0.01)
        );

        System.out.println("✅ Find by ID test passed");
    }

    @Test
    void testFindAllCourses() {
        System.out.println("🧪 REPO TEST 3: Find All Courses");

        Course pythonCourse = new Course();
        pythonCourse.setName("Python Fundamentals");
        pythonCourse.setPrice(199.99);
        entityManager.persistAndFlush(pythonCourse);

        List<Course> allCourses = courseRepository.findAll();

        assertAll("Find All",
                () -> assertEquals(2, allCourses.size()),
                () -> assertTrue(allCourses.stream().anyMatch(c -> c.getName().equals("Java Programming"))),
                () -> assertTrue(allCourses.stream().anyMatch(c -> c.getName().equals("Python Fundamentals")))
        );

        System.out.println("✅ Find all test passed");
    }

    @Test
    void testDeleteCourse() {
        System.out.println("🧪 REPO TEST 4: Delete Course");

        courseRepository.deleteById(testCourse.getId());

        Optional<Course> deletedCourse = courseRepository.findById(testCourse.getId());
        assertFalse(deletedCourse.isPresent());

        System.out.println("✅ Delete test passed");
    }
}