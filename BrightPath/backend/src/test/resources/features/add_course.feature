Feature: Add Course

  Scenario: Successfully adding a new course
    Given the course name is "ReactJS"
    And the description is "Learn frontend development"
    And the start date is "2025-08-20"
    And the price is 250.0
    And the image URL is "react.png"
    When I submit the course
    Then the course should be saved successfully
    And the course name should be "ReactJS"
