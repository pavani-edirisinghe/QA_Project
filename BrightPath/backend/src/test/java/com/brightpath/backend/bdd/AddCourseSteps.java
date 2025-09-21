package com.brightpath.backend.bdd;

import com.brightpath.backend.model.Course;
import com.brightpath.backend.repository.CourseRepository;
import com.brightpath.backend.service.CourseService;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AddCourseSteps {

    private Course course;             // the course being created
    private Course savedCourse;       // the course after saving

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Before // ✅ Use Cucumber's @Before, not JUnit's @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        course = new Course(); // ✅ Properly initialize
    }

    @Given("the course name is {string}")
    public void the_course_name_is(String name) {
        course.setName(name);
    }

    @And("the description is {string}")
    public void the_description_is(String desc) {
        course.setDescription(desc);
    }

    @And("the start date is {string}")
    public void the_start_date_is(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr);
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        course.setStartDate(date);
    }

    @And("the price is {double}")
    public void the_price_is(Double price) {
        course.setPrice(price);
    }

    @And("the image URL is {string}")
    public void the_image_url_is(String url) {
        course.setImageUrl(url);
    }

    @When("I submit the course")
    public void i_submit_the_course() {
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        savedCourse = courseService.saveCourse(course);
    }

    @Then("the course should be saved successfully")
    public void the_course_should_be_saved_successfully() {
        assertNotNull(savedCourse);
    }

    @And("the course name should be {string}")
    public void the_course_name_should_be(String expectedName) {
        assertEquals(expectedName, savedCourse.getName());
    }
}
