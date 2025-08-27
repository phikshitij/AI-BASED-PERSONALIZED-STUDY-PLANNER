package com.project.studyPlanner.controller;

import com.project.studyPlanner.models.StudyPlan;
import com.project.studyPlanner.models.DayWiseSchedule;
import com.project.studyPlanner.service.DBServices;
import com.project.studyPlanner.service.DayWiseScheduleService;
import com.project.studyPlanner.ai.GeminiDayWiseService;
import com.project.studyPlanner.models.QuizScore;
import com.project.studyPlanner.service.QuizScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/daywise")
public class DayWiseScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(DayWiseScheduleController.class);

    @Autowired
    private DBServices dbServices;

    @Autowired
    private DayWiseScheduleService dayWiseScheduleService;

    @Autowired
    private GeminiDayWiseService geminiDayWiseService;

    @Autowired
    private QuizScoreService quizScoreService;

    /**
     * ✅ Generates a day-wise study schedule for a subject.
     */
    @PostMapping("/generate")
    public Map<String, Object> generateDayWiseSchedule(@RequestBody Map<String, String> requestData) {
        String subject = requestData.get("subject");
        String userId = requestData.get("userId");

        logger.info("Received schedule generation request for subject='{}', userId='{}'", subject, userId);

        // 🛠 Validate Input
        if (subject == null || subject.isEmpty() || userId == null || userId.isEmpty()) {
            return Map.of("error", "Missing required parameters: subject and/or userId");
        }

        // 🛠 Check if the study plan exists (multi-result, robust)
        // Always fetch study plan by subject only, ignore userId
        java.util.List<StudyPlan> studyPlans = dbServices.getStudyPlansBySubject(subject);
        logger.info("Study plans found for subject '{}': {}", subject, studyPlans);
        if (studyPlans == null || studyPlans.isEmpty()) {
            logger.warn("Study plan not found for subject '{}'.", subject);
            return Map.of("error", "No study plan found for subject: " + subject + ". Please create a study plan for this subject before generating a pod or quiz.");
        }
        // Pick the first study plan (or handle as needed)
        StudyPlan studyPlan = studyPlans.get(0);

        // 🛠 Check if the schedule already exists for this user and subject
        List<DayWiseSchedule> existingSchedules = dayWiseScheduleService.findAllByUserIdAndSubject(userId, subject);
        if (!existingSchedules.isEmpty()) {
            logger.info("Schedule already exists for user '{}' and subject '{}'.", userId, subject);
            return Map.of("message", "Schedule already generated. Fetching today's schedule.");
        }

        // 🛠 Generate the day-wise schedule via Gemini AI (user and subject)
        DayWiseSchedule generatedSchedule = geminiDayWiseService.generateAndStoreDayWiseSchedule(subject, null);
        // Set userId and subject on the new schedule
        if (generatedSchedule != null) {
            generatedSchedule.setUserId(userId);
            generatedSchedule.setSubject(subject);
            DayWiseSchedule newSchedule = dayWiseScheduleService.saveSchedule(generatedSchedule);
            // After schedule creation, generate quiz if not already present
            List<QuizScore> existingQuizScores = quizScoreService.getScoresByUserAndSubject(userId, subject);
            if (existingQuizScores == null || existingQuizScores.isEmpty()) {
                QuizScore quizScore = new QuizScore(userId, subject, 3, 0); // totalQuestions=3, correctAnswers=0
                logger.info("Attempting to save QuizScore for user '{}' and subject '{}': {}", userId, subject, quizScore);
                try {
                    QuizScore savedQuiz = quizScoreService.saveQuizScore(quizScore);
                    logger.info("QuizScore successfully saved with id: {}", savedQuiz.getId());
                } catch (Exception e) {
                    logger.error("Failed to save QuizScore for user '{}' and subject '{}': {}", userId, subject, e.getMessage(), e);
                }
            } else {
                logger.info("QuizScore already exists for user '{}' and subject '{}'", userId, subject);
            }
            logger.info("Successfully generated schedule for subject '{}', user '{}'", subject, userId);
            return Map.of("message", "Schedule successfully generated!", "schedule", newSchedule);
        } else {
            logger.error("Failed to generate schedule for subject '{}', user '{}'", subject, userId);
            return Map.of("error", "Failed to generate schedule.");
        }
    }

    /**
     * ✅ Fetches today's schedule for a subject (subject-based only).
     */
    @GetMapping("/today")
    public Map<String, Object> getTodaySchedule(@RequestParam(required = false) String subject, @RequestParam(required = false) String userId) {
        // Try to fetch subject dynamically if not provided
        if (subject == null || subject.trim().isEmpty()) {
            logger.info("No subject provided. Attempting to fetch latest subject from study plans.");
            // Try to get userId from param, else fallback to default (if needed)
            String resolvedUserId = (userId != null && !userId.trim().isEmpty()) ? userId : "01";
            // Fetch the latest study plan for the user
            java.util.List<com.project.studyPlanner.models.StudyPlan> plans = dbServices.getAllStudyPlans();
            if (plans == null || plans.isEmpty()) {
                logger.warn("No study plans found in the system.");
                return Map.of("error", "No subject provided and no study plans found.");
            }
            // Optionally, filter by userId if you want personalized fallback
            java.util.Optional<com.project.studyPlanner.models.StudyPlan> latestPlan = plans.stream()
                .filter(p -> p.getUserId() != null && p.getUserId().equals(resolvedUserId))
                .findFirst();
            if (latestPlan.isPresent()) {
                subject = latestPlan.get().getSubject();
                logger.info("Dynamically resolved subject '{}' for userId '{}' from latest study plan.", subject, resolvedUserId);
            } else {
                // Fallback to any available subject
                subject = plans.get(0).getSubject();
                logger.info("Dynamically resolved subject '{}' from first available study plan.", subject);
            }
        }

        logger.info("Fetching today's schedule for subject '{}'", subject);
        LocalDate today = LocalDate.now();
        Map<String, Object> response = dayWiseScheduleService.getTodayScheduleBySubject(subject, today);

        if (response.containsKey("message")) {
            logger.warn("No schedule found for today for subject '{}'", subject);
        } else {
            logger.info("Successfully fetched today's schedule for subject '{}'", subject);
        }

        return response;
    }
}
