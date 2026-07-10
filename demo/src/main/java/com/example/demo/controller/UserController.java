package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createOrLoginUser(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String email = body.get("email");

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email is required"));
            }

            username = username.trim();
            email = email.trim();

            Optional<User> existingUser = userRepository.findByEmail(email);

            if (existingUser.isPresent()) {
                return ResponseEntity.ok(existingUser.get());
            }

            Optional<User> userWithSameUsername = userRepository.findByUsername(username);
            if (userWithSameUsername.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username already taken"));
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user = userRepository.save(user);

            System.out.println("Created new user: " + user.getUsername() + " (ID: " + user.getId() + ")");

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            }
            return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            }
            return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}