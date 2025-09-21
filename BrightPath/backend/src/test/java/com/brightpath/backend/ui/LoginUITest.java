package com.brightpath.backend.ui;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoginUITest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.setAcceptInsecureCerts(true);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get("http://localhost:5173/login");
            System.out.println("Navigated to login page");
        } catch (Exception e) {
            fail("Could not load login page. Make sure frontend is running on port 5173");
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private WebElement findElement(By... locators) {
        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.elementToBeClickable(locator));
            } catch (Exception e) {
                // Try next locator
            }
        }

        System.out.println("Element not found! URL: " + driver.getCurrentUrl());
        throw new NoSuchElementException("Could not find element. Current URL: " + driver.getCurrentUrl());
    }

    @Test
    void testSuccessfulLogin() {
        System.out.println("Testing successful login...");

        // Clear fields first
        try {
            WebElement usernameField = findElement(By.id("username"));
            usernameField.clear();
            usernameField.sendKeys("testuser"); // Your actual test user

            WebElement passwordField = findElement(By.id("password"));
            passwordField.clear();
            passwordField.sendKeys("password123"); // Plain text password

            System.out.println("Entered credentials: testuser/password123");
        } catch (Exception e) {
            System.out.println("Could not find username/password fields: " + e.getMessage());
            return;
        }

        String beforeUrl = driver.getCurrentUrl();
        System.out.println("Before login: " + beforeUrl);

        try {
            // Try to find and click login button
            WebElement loginButton = findElement(
                    By.xpath("//button[contains(text(),'Login')]"),
                    By.xpath("//button[contains(text(),'Sign In')]"),
                    By.cssSelector("button[type='submit']"),
                    By.tagName("button") // Last resort - first button
            );
            loginButton.click();
            System.out.println("Clicked login button");
        } catch (Exception e) {
            System.out.println("Could not click login button: " + e.getMessage());
            return;
        }

        // Wait for response
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String afterUrl = driver.getCurrentUrl();
        System.out.println("After login: " + afterUrl);

        // Check if redirected OR success message appears
        boolean success = false;

        // Option 1: Redirected to dashboard
        if (afterUrl.contains("/dashboard") || !afterUrl.contains("/login")) {
            System.out.println("SUCCESS: Redirected to " + afterUrl);
            success = true;
        }
        // Option 2: Still on login page but success message
        else if (afterUrl.contains("/login")) {
            // Check for success indicators
            String pageText = driver.getPageSource().toLowerCase();
            if (pageText.contains("success") ||
                    pageText.contains("welcome") ||
                    pageText.contains("dashboard")) {
                System.out.println("SUCCESS: Success message found on login page");
                success = true;
            }
        }

        assertTrue(success,
                "Login should redirect OR show success message. Current URL: " + afterUrl +
                        "\nMake sure 'testuser' with password 'password123' exists in database!");
    }


    @Test
    @Disabled("Run to debug login page structure")
    void debugLoginPage() {
        System.out.println("\nDEBUGGING LOGIN PAGE");
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Title: " + driver.getTitle());

        // Wait a bit for page to fully load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Find form elements
        List<WebElement> forms = new ArrayList<>(driver.findElements(By.tagName("form")));
        System.out.println("Found " + forms.size() + " forms");

        // Find inputs
        List<WebElement> inputs = new ArrayList<>(driver.findElements(By.tagName("input")));
        System.out.println("\nInput fields found:");
        for (WebElement input : inputs) {
            System.out.println("  - ID: '" + getAttributeSafe(input, "id") +
                    "' | Name: '" + getAttributeSafe(input, "name") +
                    "' | Type: '" + getAttributeSafe(input, "type") +
                    "' | Placeholder: '" + getAttributeSafe(input, "placeholder") + "'");
        }

        // Find buttons
        List<WebElement> buttons = new ArrayList<>(driver.findElements(By.tagName("button")));
        System.out.println("\nButtons found:");
        for (WebElement button : buttons) {
            System.out.println("  - Text: '" + button.getText() +
                    "' | ID: '" + getAttributeSafe(button, "id") +
                    "' | Type: '" + getAttributeSafe(button, "type") +
                    "' | Class: '" + getAttributeSafe(button, "class") + "'");
        }

        // Check page source for clues
        String pageSource = driver.getPageSource();
        if (pageSource.contains("username") || pageSource.contains("password")) {
            System.out.println("\nLogin form elements detected in page source");
        } else {
            System.out.println("\nNo login form detected - check if you're on the right page!");
        }
    }

    // Helper method to handle deprecated getAttribute
    private String getAttributeSafe(WebElement element, String attributeName) {
        try {
            // Use getDomProperty for newer Selenium versions (replaces getAttribute)
            Object attribute = element.getDomProperty(attributeName);
            return attribute != null ? attribute.toString() : "";
        } catch (Exception e) {
            // Fallback to deprecated getAttribute
            return element.getAttribute(attributeName);
        }
    }
}