package com.jobtracker.controller;

import com.jobtracker.model.User;
import com.jobtracker.model.Admin;
import com.jobtracker.repository.UserRepository;
import com.jobtracker.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // Added
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5174")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/register")
    @Transactional // Added
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User(username, password, "user");
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/admin/register")
    @Transactional // Added
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        Optional<Admin> existingAdmin = adminRepository.findByUsername(username);
        if (existingAdmin.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        Admin admin = new Admin(username, password, "admin");
        adminRepository.save(admin);
        return ResponseEntity.ok("Admin registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String role = request.get("role");

        if (username == null || password == null || role == null) {
            return ResponseEntity.badRequest().body("Username, password, and role are required");
        }

        Map<String, Object> response = new HashMap<>();

        if (role.equals("admin")) {
            Optional<Admin> admin = adminRepository.findByUsername(username);
            if (admin.isPresent() && admin.get().getPassword().equals(password)) {
                response.put("username", username);
                response.put("role", "admin");
                return ResponseEntity.ok(response);
            }
        } else if (role.equals("user")) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent() && user.get().getPassword().equals(password)) {
                response.put("username", username);
                response.put("role", "user");
                response.put("userId", user.get().getId());
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.badRequest().body("Invalid username or password");
    }
}