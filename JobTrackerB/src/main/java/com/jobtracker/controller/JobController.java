package com.jobtracker.controller;

import com.jobtracker.model.Job;
import com.jobtracker.model.User;
import com.jobtracker.repository.JobRepository;
import com.jobtracker.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:5174")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllJobs(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        System.out.println("GET /api/jobs - Role from JWT: " + role);
        if (role == null) {
            return ResponseEntity.status(401).body("No role found in token - please log in again");
        }
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body("Unauthorized: Only admins can view all jobs");
        }
        try {
            List<Job> jobs = jobRepository.findAll();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.out.println("Error in getAllJobs: " + e.getMessage());
            return ResponseEntity.status(500).body("Error retrieving jobs: " + e.getMessage());
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createJob(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        System.out.println("POST /api/jobs - Starting createJob method...");
        System.out.println("Request Body: " + request);

        String company = (String) request.get("company");
        String position = (String) request.get("position");
        String dateStr = (String) request.get("date");
        String status = (String) request.get("status");

        String role = (String) httpRequest.getAttribute("role");
        Long userId = (Long) httpRequest.getAttribute("userId");
        System.out.println("Role from JWT: " + role + ", UserId from JWT: " + userId);

        // Validate role and userId (from JWT)
        if (role == null) {
            return ResponseEntity.status(401).body("No role found in token - please log in again");
        }
        if (!"user".equals(role)) {
            return ResponseEntity.status(403).body("Only users can add jobs");
        }

        if (userId == null) {
            return ResponseEntity.status(401).body("User ID not found in token - please log in again");
        }

        // Validate required fields
        if (company == null || position == null || dateStr == null || status == null) {
            return ResponseEntity.badRequest().body("Required fields: company, position, date, status");
        }

        // Check if user exists
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found with id: " + userId);
        }

        // Validate date format
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format: use YYYY-MM-DD (e.g., 2025-05-10)");
        }

        // Create and save the job
        try {
            Job job = new Job();
            job.setCompany(company);
            job.setPosition(position);
            job.setDate(date);
            job.setStatus(status);
            job.setUser(user.get());

            Job savedJob = jobRepository.save(job);
            return ResponseEntity.ok(savedJob);
        } catch (Exception e) {
            System.out.println("Error in createJob: " + e.getMessage());
            return ResponseEntity.status(500).body("Error creating job: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        System.out.println("PUT /api/jobs/" + id + " - Role from JWT: " + role);
        if (role == null) {
            return ResponseEntity.status(401).body("No role found in token - please log in again");
        }
        if (!"user".equals(role)) {
            return ResponseEntity.status(403).body("Only users can update jobs");
        }

        Optional<Job> jobOptional = jobRepository.findById(id);
        if (!jobOptional.isPresent()) {
            return ResponseEntity.status(404).body("Job not found with id: " + id);
        }

        Job job = jobOptional.get();
        String company = (String) request.get("company");
        String position = (String) request.get("position");
        String dateStr = (String) request.get("date");
        String status = (String) request.get("status");

        try {
            if (company != null) job.setCompany(company);
            if (position != null) job.setPosition(position);
            if (dateStr != null) {
                try {
                    job.setDate(LocalDate.parse(dateStr));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid date format: use YYYY-MM-DD (e.g., 2025-05-10)");
                }
            }
            if (status != null) job.setStatus(status);

            Job updatedJob = jobRepository.save(job);
            return ResponseEntity.ok(updatedJob);
        } catch (Exception e) {
            System.out.println("Error in updateJob: " + e.getMessage());
            return ResponseEntity.status(500).body("Error updating job: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteJob(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        System.out.println("DELETE /api/jobs/" + id + " - Role from JWT: " + role);
        if (role == null) {
            return ResponseEntity.status(401).body("No role found in token - please log in again");
        }
        if (!"user".equals(role)) {
            return ResponseEntity.status(403).body("Only users can delete jobs");
        }

        Optional<Job> jobOptional = jobRepository.findById(id);
        if (!jobOptional.isPresent()) {
            return ResponseEntity.status(404).body("Job not found with id: " + id);
        }

        try {
            jobRepository.delete(jobOptional.get());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error in deleteJob: " + e.getMessage());
            return ResponseEntity.status(500).body("Error deleting job: " + e.getMessage());
        }
    }
}