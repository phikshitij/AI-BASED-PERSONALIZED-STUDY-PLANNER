package com.project.studyPlanner.controller;

import com.project.studyPlanner.models.OnboardingData;
import com.project.studyPlanner.service.OnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "*")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @PostMapping("/save")
    public ResponseEntity<?> saveOnboardingData(@RequestBody OnboardingData onboardingData) {
        try {
            // Always set userId to "01"
            onboardingData.setUserId("01");
            
            // Check for existing data and update instead of creating new
            OnboardingData savedData = onboardingService.saveOrUpdateOnboardingData(onboardingData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Onboarding data saved successfully");
            response.put("data", savedData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to save onboarding data");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/fetch/{userId}")
    public ResponseEntity<?> getOnboardingData(@PathVariable String userId) {
        try {
            OnboardingData data = onboardingService.getOnboardingDataByUserId(userId);
            if (data != null) {
                return ResponseEntity.ok(data);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "No onboarding data found");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch onboarding data");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
