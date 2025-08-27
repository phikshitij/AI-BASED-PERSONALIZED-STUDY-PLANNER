package com.project.studyPlanner.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RecommendationService {

    public String generateRecommendations(Map<String, Double> quizScores, Map<String, Integer> podCompletion) {
        StringBuilder recommendations = new StringBuilder();

        // ✅ Debugging - Print fetched data
        System.out.println("Quiz Scores: " + quizScores);
        System.out.println("Pod Completion: " + podCompletion);

        // ✅ Analyze quiz performance
        for (String subject : quizScores.keySet()) {
            double score = quizScores.get(subject);
            String topic = (subject != null) ? subject : "a weak topic"; // Prevent "undefined"

            if (score < 50) { // If score is below 50%, suggest revision
                recommendations.append("🔍 Revise ").append(topic).append(" topics.\n");
                recommendations.append("📖 Suggested resource: [Watch Here](")
                        .append(getResourceLink(topic)).append(").\n\n");
            }
        }

        // ✅ Analyze pod completion
        for (String subject : podCompletion.keySet()) {
            int podsCompleted = podCompletion.get(subject);
            if (podsCompleted < 3) { // If less than 3 pods completed, suggest additional practice
                recommendations.append("📌 Complete more learning pods for ").append(subject).append(".\n\n");
            }
        }

        return recommendations.length() > 0 ? recommendations.toString() : "✅ You're on track! Keep up the great work!";
    }

    // ✅ Get resource link based on subject
    private String getResourceLink(String subject) {
        Map<String, String> resourceLinks = Map.of(
            "Physics", "https://www.example.com/physics-video",
            "Math", "https://www.example.com/math-video",
            "Chemistry", "https://www.example.com/chemistry-video"
        );
        return resourceLinks.getOrDefault(subject, "https://www.example.com/general-learning");
    }
}
