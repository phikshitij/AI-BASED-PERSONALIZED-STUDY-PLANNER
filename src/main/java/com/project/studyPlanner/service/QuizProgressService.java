package com.project.studyPlanner.service;

import com.project.studyPlanner.repository.QuizProgressRepository;
import com.project.studyPlanner.repository.QuizScoreRepository;
import com.project.studyPlanner.models.QuizScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizProgressService {

    @Autowired
    private QuizProgressRepository quizProgressRepository;

    @Autowired
    private QuizScoreRepository quizScoreRepository;

    public Map<String, Double> fetchQuizScores(String userId) {
        List<QuizScore> quizScores = quizScoreRepository.findByUserId(userId);
        Map<String, Double> scoreMap = new LinkedHashMap<>();
        for (QuizScore score : quizScores) {
            double percent = (score.getTotalQuestions() > 0)
                ? ((double) score.getCorrectAnswers() / score.getTotalQuestions()) * 100
                : 0.0;
            String subject = score.getSubject();
            // Keep the max score for each subject
            scoreMap.put(subject, Math.max(scoreMap.getOrDefault(subject, 0.0), percent));
        }
        System.out.println("Returning quiz scores to frontend: " + scoreMap);
        return scoreMap;
    }
}
