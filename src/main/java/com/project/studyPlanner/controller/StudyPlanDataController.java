package com.project.studyPlanner.controller;

import com.project.studyPlanner.models.StudyPlanData;
import com.project.studyPlanner.service.StudyPlanDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/studyplan/data")
@CrossOrigin(origins = "*")
public class StudyPlanDataController {

    @Autowired
    private StudyPlanDataService studyPlanDataService;

    @GetMapping("/fetch")
    public ResponseEntity<Map<String, Object>> getUserStudyPlanData() {
        String userId = "01"; // Fixed user ID for now
        Optional<StudyPlanData> optionalData = studyPlanDataService.getUserStudyPlanData(userId);

        if (optionalData.isPresent()) {
            StudyPlanData data = optionalData.get();
            Map<String, Object> response = new HashMap<>();
            response.put("academicLevel", data.getAcademicLevel());
            response.put("domain", data.getDomain());
            response.put("year", data.getYear());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
