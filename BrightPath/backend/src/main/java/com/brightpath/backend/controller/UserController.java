package com.brightpath.backend.controller;

import com.brightpath.backend.model.User;
import com.brightpath.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Frontend URL
public class UserController {

    @Autowired
    private UserService userService;

    // Create a fixed upload directory in your project folder
    @Value("${file.upload-dir}") // Read from application.properties
    private String uploadDir;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) throws IOException {
        if (userService.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already taken"));
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        if (profileImage != null && !profileImage.isEmpty()) {
            // Use absolute path
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            profileImage.transferTo(filePath.toFile());
            user.setProfileImage(fileName);
        }

        User saved = userService.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully", "user", saved));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        Optional<User> optionalUser = userService.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(password)) {
                return ResponseEntity.ok(Map.of(
                        "message", "Login successful",
                        "user", user,
                        "token", UUID.randomUUID().toString()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid password"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser
    ) {
        Optional<User> existingUserOpt = userService.findByIdOptional(id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        User existingUser = existingUserOpt.get();
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());

        // Optional: update password only if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        User savedUser = userService.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}/profile-image")
    public ResponseEntity<?> updateProfileImage(
            @PathVariable Long id,
            @RequestParam("profileImage") MultipartFile profileImage
    ) throws IOException {
        System.out.println("Uploading profile image for user id: " + id);

        Optional<User> optionalUser = userService.findByIdOptional(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        if (profileImage == null || profileImage.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No image uploaded"));
        }

        User user = optionalUser.get();

        // Save the image
        String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName);
        profileImage.transferTo(filePath.toFile());

        user.setProfileImage(fileName);
        User saved = userService.save(user);

        return ResponseEntity.ok(Map.of("profileImage", saved.getProfileImage()));
    }


}
