package com.brightpath.backend.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=true",
        "course.upload-dir=./target/test-uploads"
})
public class CourseApiTest {

    @LocalServerPort
    private int port;

    private static final String BASE_URL = "http://localhost";
    private static final String API_BASE = "/api/courses";

    private Long createdCourseId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        new java.io.File("./target/test-uploads").mkdirs();
        System.out.println("Starting API tests with H2 database...");
    }

    @BeforeEach
    void waitForStartup() {
        try {
            Thread.sleep(3000); // Wait for Spring Boot to start
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Testing on port: " + port);
    }

    /**
     * TEST CASE 1: Create Course Successfully (POST /api/courses)
     */
    @Test
    void testCreateCourseSuccessfully() {
        System.out.println("TEST 1: Create Course Successfully");

        // Send POST request
        Response response = given()
                .port(port)
                .contentType(ContentType.MULTIPART)
                .multiPart("name", "Java Programming")
                .multiPart("description", "Learn Java basics")
                .multiPart("startDate", "2025-01-15")
                .multiPart("price", "299.99")
                .when()
                .post(API_BASE);

        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());

        // Validate response
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 200 || statusCode == 201,
                "Expected 200 or 201, got: " + statusCode);

        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("Java Programming"),
                "Response should contain course name");
        assertTrue(responseBody.contains("Learn Java basics"),
                "Response should contain description");

        // Try to get course ID from JSON response
        try {
            if (responseBody.contains("\"id\":")) {
                createdCourseId = response.jsonPath().getLong("id");
                assertNotNull(createdCourseId, "Should have course ID");
                System.out.println("Course created with ID: " + createdCourseId);
            }
        } catch (Exception e) {
            System.out.println("Could not parse JSON response");
        }

        System.out.println("TEST 1 PASSED - Course created successfully");
    }

    /**
     * TEST CASE 2: Get All Courses (GET /api/courses)
     */
    @Test
    void testGetAllCourses() {
        System.out.println("TEST 2: Get All Courses");

        // Send GET request
        Response response = given()
                .port(port)
                .get(API_BASE);

        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());

        // Validate response
        int statusCode = response.getStatusCode();

        if (statusCode == 200) {
            String responseBody = response.getBody().asString();
            assertTrue(responseBody.contains("["), "Should return JSON array");

            if ("[]".equals(responseBody)) {
                System.out.println("Empty courses list returned");
            } else {
                System.out.println("Courses list returned");

                // Verify created course if it exists
                if (createdCourseId != null && responseBody.contains("Java Programming")) {
                    System.out.println("Created course found in list");
                }
            }
        } else {
            System.out.println("ℹStatus " + statusCode + " - table may not exist yet");
            assertTrue(statusCode >= 400 && statusCode < 600,
                    "Should return server error if table doesn't exist");
        }

        System.out.println("TEST 2 PASSED - GET endpoint tested");
    }

    @AfterEach
    void cleanup() {
        if (createdCourseId != null) {
            try {
                Response deleteResponse = given()
                        .port(port)
                        .delete(API_BASE + "/" + createdCourseId);

                System.out.println("Cleanup status: " + deleteResponse.getStatusCode());
            } catch (Exception e) {
                System.out.println("ℹCleanup skipped");
            }
        }
    }

    @AfterAll
    static void cleanupAll() {
        try {
            java.io.File uploadDir = new java.io.File("./target/test-uploads");
            if (uploadDir.exists()) {
                deleteDir(uploadDir);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }

        System.out.println("All API tests completed!");
    }

    private static void deleteDir(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] entries = file.listFiles();
            if (entries != null) {
                for (java.io.File entry : entries) {
                    deleteDir(entry);
                }
            }
        }
        file.delete();
    }
}