package com.project.studyPlanner.controller;

import com.project.studyPlanner.service.QuizProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progressTracking")
@CrossOrigin(origins = "*") // Allow frontend access
public class QuizProgressController {

    @Autowired
    private QuizProgressService quizProgressService;

    // ✅ Endpoint to fetch quiz scores
    @GetMapping("/quizScores")
    public Map<String, Double> getQuizScores(@RequestParam String userId) {
        return quizProgressService.fetchQuizScores(userId);
    }
}
