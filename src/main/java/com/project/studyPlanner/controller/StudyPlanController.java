package com.project.studyPlanner.controller;

import com.project.studyPlanner.models.StudyPlan;
import com.project.studyPlanner.repository.StudyPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StudyPlanController {

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    // ✅ Save Study Plan (Returns JSON instead of plain text)
    @PostMapping("/studyplan/save")
    public ResponseEntity<Map<String, Object>> saveStudyPlan(@RequestBody StudyPlan studyPlan) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate required fields
            if (studyPlan.getSubject() == null || studyPlan.getSubject().isEmpty()) {
                response.put("error", "Subject is required");
                return ResponseEntity.badRequest().body(response);
            }
            if (studyPlan.getModule() == null || studyPlan.getModule().isEmpty()) {
                response.put("error", "Module is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Set default values if not provided
            if (studyPlan.getUserId() == null || studyPlan.getUserId().isEmpty()) {
                studyPlan.setUserId("default");
            }
            if (studyPlan.getCreatedAt() == null) {
                studyPlan.setCreatedAt(ZonedDateTime.now());
            }

            // Delete any existing plan for the same user, subject, and module
            List<StudyPlan> existingPlans = studyPlanRepository.findAllByUserIdAndSubjectAndModule(
                studyPlan.getUserId(), studyPlan.getSubject(), studyPlan.getModule());
            for (StudyPlan plan : existingPlans) {
                studyPlanRepository.deleteById(plan.getId());
            }

            // Save to database
            StudyPlan saved = studyPlanRepository.save(studyPlan);

            // ✅ Return JSON response
            response.put("message", "Study Plan Saved Successfully");
            response.put("id", saved.getId());
            response.put("studyPlan", saved);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Log the full error

            response.put("error", "Error saving study plan");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ✅ Get All Study Plans
    @GetMapping("/studyplan/all")
    public ResponseEntity<List<StudyPlan>> getAllStudyPlans() {
        return ResponseEntity.ok(studyPlanRepository.findAll());
    }

    // ✅ Get Study Plan by ID
    @GetMapping("/studyplan/{id}")
    public ResponseEntity<Map<String, Object>> getStudyPlanById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return studyPlanRepository.findById(id)
                .map(plan -> {
                    response.put("message", "Study Plan Found");
                    response.put("studyPlan", plan);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("error", "Study Plan not found");
                    return ResponseEntity.badRequest().body(response);
                });
    }

    // ✅ Delete Study Plan by ID
    @DeleteMapping("/studyplan/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudyPlan(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        if (studyPlanRepository.existsById(id)) {
            studyPlanRepository.deleteById(id);
            response.put("message", "Study Plan deleted successfully!");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Study Plan not found!");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ✅ Get Study Plans by User ID
    @GetMapping("/studyplan/study-plans")
    public ResponseEntity<List<StudyPlan>> getStudyPlansByUserId(@RequestParam(required = false) String userId) {
        List<StudyPlan> studyPlans;

        if (userId == null || userId.isEmpty()) {
            studyPlans = studyPlanRepository.findAll();
        } else {
            studyPlans = studyPlanRepository.findAll().stream()
                    .filter(plan -> plan.getUserId() != null && plan.getUserId().equals(userId))
                    .collect(Collectors.toList());

            if (studyPlans.isEmpty()) {
                studyPlans = studyPlanRepository.findAll();
            }
        }

        return ResponseEntity.ok(studyPlans);
    }

    // NEW: Get Study Plan by user, subject, and module
    @GetMapping("/studyplan/user/{userId}/subject/{subject}/module/{module}")
    public ResponseEntity<Map<String, Object>> getStudyPlanByUserSubjectModule(@PathVariable String userId, @PathVariable String subject, @PathVariable String module) {
        Map<String, Object> response = new HashMap<>();
        List<StudyPlan> plans = studyPlanRepository.findAllByUserIdAndSubjectAndModule(userId, subject, module);
        if (plans.isEmpty()) {
            response.put("error", "Study Plan not found");
            return ResponseEntity.badRequest().body(response);
        } else {
            response.put("message", "Study Plan(s) Found");
            response.put("studyPlans", plans);
            return ResponseEntity.ok(response);
        }
    }
}
