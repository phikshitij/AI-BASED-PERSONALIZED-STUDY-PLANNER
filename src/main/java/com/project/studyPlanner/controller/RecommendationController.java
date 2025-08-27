package com.project.studyPlanner.controller;

import com.project.studyPlanner.service.QuizProgressService;
import com.project.studyPlanner.service.PodCompletionService;
import com.project.studyPlanner.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progressTracking")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private QuizProgressService quizProgressService; // ✅ Injected instance

    @Autowired
    private PodCompletionService podCompletionService; // ✅ Injected instance

    @Autowired
    private RecommendationService recommendationService; // ✅ Injected instance

    // ✅ Generate AI-based recommendations
    @GetMapping("/recommendations")
    public String getRecommendations(@RequestParam String userId) {
        // ✅ Fetch data using instance methods (not static calls)
        Map<String, Double> quizScores = quizProgressService.fetchQuizScores(userId); 
        Map<String, Integer> podCompletion = podCompletionService.fetchPodCompletionData(); 

        return recommendationService.generateRecommendations(quizScores, podCompletion);
    }
}
