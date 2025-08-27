package com.project.studyPlanner.controller;

import com.project.studyPlanner.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "http://localhost:5500")  // Adjust for your frontend URL
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @PostMapping("/completePod")
    public ResponseEntity<String> completePod(@RequestBody Map<String, String> request) {
        String subject = request.get("subject");
        String podTitle = request.get("podTitle");
        String userId = "01";  // 🔹 Hardcoded User ID

        if (subject == null || podTitle == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        progressService.markPodAsCompleted(userId, subject, podTitle);
        return ResponseEntity.ok("Pod marked as completed successfully");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserProgress() {
        String userId = "01";  // 🔹 Hardcoded User ID
        return ResponseEntity.ok(progressService.getUserProgress(userId));
    }
}
