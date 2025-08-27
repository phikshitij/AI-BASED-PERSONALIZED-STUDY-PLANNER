package com.project.studyPlanner.controller;

import com.project.studyPlanner.service.PodCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progressTracking")
@CrossOrigin(origins = "*") // ✅ Allow frontend access
public class PodCompletionController {

    @Autowired
    private PodCompletionService podCompletionService;

    // ✅ API Endpoint to fetch pod completion data
    @GetMapping("/podCompletion")
    public Map<String, Integer> getPodCompletionData() {
        return podCompletionService.fetchPodCompletionData();
    }
}
