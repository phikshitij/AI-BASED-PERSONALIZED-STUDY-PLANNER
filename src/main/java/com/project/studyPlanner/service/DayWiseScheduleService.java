package com.project.studyPlanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.studyPlanner.models.DayWiseSchedule;
import com.project.studyPlanner.repository.DayWiseScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DayWiseScheduleService {
    // Fetch all schedules for a given subject (ignore userId)
    public List<DayWiseSchedule> findAllBySubject(String subject) {
        return dayWiseScheduleRepository.findAllBySubject(subject);
    }

    private static final Logger logger = LoggerFactory.getLogger(DayWiseScheduleService.class);

    @Autowired
    private DayWiseScheduleRepository dayWiseScheduleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public List<DayWiseSchedule> findAllByUserIdAndSubject(String userId, String subject) {
        return dayWiseScheduleRepository.findAllByUserIdAndSubject(userId, subject);
    }

    public DayWiseSchedule saveSchedule(DayWiseSchedule schedule) {
        return dayWiseScheduleRepository.save(schedule);
    }

    // Utility method to check if a string is valid JSON
    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Fetch today's schedule for a subject (subject-based only, ignores userId).
     */
    public Map<String, Object> getTodayScheduleBySubject(String subject, LocalDate today) {
        logger.info("Fetching today's schedule for subject '{}' on date '{}'", subject, today);
        List<DayWiseSchedule> schedules = findAllBySubject(subject);

        if (schedules.isEmpty()) {
            logger.warn("No schedules found for subject '{}'", subject);
            return Map.of("message", "No schedules found for subject.");
        }

        List<Map<String, Object>> todaySchedules = new ArrayList<>();

        for (DayWiseSchedule schedule : schedules) {
            if (schedule.getStartDate() == null) {
                continue;
            }

            long daysElapsed = ChronoUnit.DAYS.between(schedule.getStartDate(), today);
            String dayKey = "Day " + (daysElapsed + 1);

            try {
                // Sanitize malformed links in the JSON before parsing
                String sanitizedData = schedule.getDayWiseData().replaceAll("https://www\\.https://", "https://");
                if (!isValidJson(sanitizedData)) {
                    logger.error("Invalid JSON for schedule ID '{}': {}", schedule.getId(), sanitizedData);
                    todaySchedules.add(Map.of("error", "Malformed JSON data for schedule. Please contact support.", "scheduleId", schedule.getId()));
                    continue;
                }
                JsonNode scheduleJson = objectMapper.readTree(sanitizedData);

                if (scheduleJson.has(dayKey)) {
                    JsonNode todaySchedule = scheduleJson.get(dayKey);
                    JsonNode pods = todaySchedule.get("pods");

                    List<Map<String, Object>> podList = new ArrayList<>();
                    List<Map<String, Object>> quizList = new ArrayList<>();

                    if (pods != null && pods.isArray()) {
                        for (JsonNode pod : pods) {
                            List<String> keyConcepts = new ArrayList<>();
                            if (pod.has("key_concepts") && pod.get("key_concepts").isArray()) {
                                for (JsonNode concept : pod.get("key_concepts")) {
                                    keyConcepts.add(concept.asText());
                                }
                            }

                            if (pod.has("quiz") && pod.get("quiz").isArray()) {
                                for (JsonNode quiz : pod.get("quiz")) {
                                    Map<String, Object> quizData = new HashMap<>();
                                    quizData.put("question", quiz.get("question").asText());
                                    quizData.put("options", quiz.get("options"));
                                    quizData.put("answer", quiz.get("answer").asText());
                                    quizList.add(quizData);
                                }
                            }

                            List<Map<String, String>> resources = new ArrayList<>();
                            if (pod.has("resources") && pod.get("resources").isArray()) {
                                for (JsonNode resource : pod.get("resources")) {
                                    Map<String, String> resourceData = new HashMap<>();
                                    resourceData.put("title", resource.has("title") ? resource.get("title").asText() : "Resource");
                                    resourceData.put("link", resource.has("link") ? resource.get("link").asText() : "#");
                                    resources.add(resourceData);
                                }
                            }

                            Map<String, Object> podData = new HashMap<>();
                            podData.put("title", pod.get("title").asText());
                            podData.put("key_concepts", keyConcepts);
                            podData.put("resources", resources);
                            podList.add(podData);
                        }
                    }

                    Map<String, Object> scheduleData = new HashMap<>();
                    scheduleData.put("topic", schedule.getSubject());
                    scheduleData.put("micro_learning_pod", podList);
                    scheduleData.put("quiz", quizList);
                    scheduleData.put("resources", podList.stream().map(pod -> pod.get("resources")).toList());

                    todaySchedules.add(scheduleData);
                }
            } catch (JsonProcessingException e) {
                logger.error("Error parsing JSON for schedule ID '{}': {}. Raw data: {}", schedule.getId(), e.getMessage(), schedule.getDayWiseData());
                todaySchedules.add(Map.of("error", "Error parsing JSON for schedule.", "scheduleId", schedule.getId()));
            }
        }

        if (todaySchedules.isEmpty()) {
            return Map.of("message", "No schedule found for today.");
        }

        return Map.of("todaySchedule", todaySchedules);
    }

    /**
     * @deprecated Use getTodayScheduleBySubject for subject-based schedule fetching.
     */
    @Deprecated
    public Map<String, Object> getTodaySchedule(String userId, LocalDate today) {
        logger.info("Fetching today's schedule for user '{}' on date '{}'", userId, today);
        List<DayWiseSchedule> schedules = dayWiseScheduleRepository.findByUserId(userId);

        if (schedules.isEmpty()) {
            logger.warn("No schedules found for user '{}'", userId);
            return Map.of("message", "No schedules found for user.");
        }

        List<Map<String, Object>> todaySchedules = new ArrayList<>();

        for (DayWiseSchedule schedule : schedules) {
            if (schedule.getStartDate() == null) {
                continue;
            }

            long daysElapsed = ChronoUnit.DAYS.between(schedule.getStartDate(), today);
            String dayKey = "Day " + (daysElapsed + 1);

            try {
                // Sanitize malformed links in the JSON before parsing
                String sanitizedData = schedule.getDayWiseData().replaceAll("https://www\\.https://", "https://");
                if (!isValidJson(sanitizedData)) {
                    logger.error("Invalid JSON for schedule ID '{}': {}", schedule.getId(), sanitizedData);
                    todaySchedules.add(Map.of("error", "Malformed JSON data for schedule. Please contact support.", "scheduleId", schedule.getId()));
                    continue;
                }
                JsonNode scheduleJson = objectMapper.readTree(sanitizedData);

                if (scheduleJson.has(dayKey)) {
                    JsonNode todaySchedule = scheduleJson.get(dayKey);
                    JsonNode pods = todaySchedule.get("pods");

                    List<Map<String, Object>> podList = new ArrayList<>();
                    List<Map<String, Object>> quizList = new ArrayList<>();

                    if (pods != null && pods.isArray()) {
                        for (JsonNode pod : pods) {
                            List<String> keyConcepts = new ArrayList<>();
                            if (pod.has("key_concepts") && pod.get("key_concepts").isArray()) {
                                for (JsonNode concept : pod.get("key_concepts")) {
                                    keyConcepts.add(concept.asText());
                                }
                            }

                            if (pod.has("quiz") && pod.get("quiz").isArray()) {
                                for (JsonNode quizItem : pod.get("quiz")) {
                                    Map<String, Object> quizMap = new HashMap<>();
                                    quizMap.put("question", quizItem.get("question").asText());
                                    List<String> options = new ArrayList<>();
                                    for (JsonNode option : quizItem.get("options")) {
                                        options.add(option.asText());
                                    }
                                    quizMap.put("options", options);
                                    quizMap.put("answer", quizItem.get("answer").asText());
                                    quizList.add(quizMap);
                                }
                            }

                            List<Map<String, String>> resources = new ArrayList<>();
                            if (pod.has("resources") && pod.get("resources").isArray()) {
                                for (JsonNode resource : pod.get("resources")) {
                                    Map<String, String> resourceData = new HashMap<>();
                                    resourceData.put("title", resource.has("title") ? resource.get("title").asText() : "Resource");
                                    resourceData.put("link", resource.has("link") ? resource.get("link").asText() : "#");
                                    resources.add(resourceData);
                                }
                            }

                            Map<String, Object> podData = new HashMap<>();
                            podData.put("title", pod.get("title").asText());
                            podData.put("key_concepts", keyConcepts);
                            podData.put("resources", resources);
                            podList.add(podData);
                        }
                    }

                    Map<String, Object> scheduleData = new HashMap<>();
                    scheduleData.put("topic", schedule.getSubject());
                    scheduleData.put("micro_learning_pod", podList);
                    scheduleData.put("quiz", quizList);
                    scheduleData.put("resources", podList.stream().map(pod -> pod.get("resources")).toList());

                    todaySchedules.add(scheduleData);
                }
            } catch (JsonProcessingException e) {
                logger.error("Error parsing JSON for schedule ID '{}': {}. Raw data: {}", schedule.getId(), e.getMessage(), schedule.getDayWiseData());
                todaySchedules.add(Map.of("error", "Error parsing JSON for schedule.", "scheduleId", schedule.getId()));
            }
        }

        if (todaySchedules.isEmpty()) {
            return Map.of("message", "No schedule found for today.");
        }

        return Map.of("todaySchedule", todaySchedules);
    }

    public Map<String, Object> getPodDetails(String userId, String subject, String podTitle) {
        // Utility method is already available for JSON validation

        List<DayWiseSchedule> existingSchedules = dayWiseScheduleRepository.findAllByUserIdAndSubject(userId, subject);
        if (existingSchedules.isEmpty()) {
            return Map.of("message", "No schedule found for this subject.");
        }

        DayWiseSchedule schedule = existingSchedules.get(0);
        try {
            // Sanitize malformed links in the JSON before parsing
            String sanitizedData = schedule.getDayWiseData().replaceAll("https://www\\.https://", "https://");
            if (!isValidJson(sanitizedData)) {
                logger.error("Invalid JSON for schedule ID '{}': {}", schedule.getId(), sanitizedData);
                return Map.of("error", "Malformed JSON data for schedule. Please contact support.", "scheduleId", schedule.getId());
            }
            JsonNode scheduleJson = objectMapper.readTree(sanitizedData);

            for (JsonNode dayNode : scheduleJson) {
                for (JsonNode pod : dayNode.get("pods")) {
                    if (pod.get("title").asText().equalsIgnoreCase(podTitle)) {
                        List<Map<String, String>> resources = new ArrayList<>();
                        if (pod.has("resources")) {
                            for (JsonNode res : pod.get("resources")) {
                                resources.add(Map.of(
                                    "title", res.get("title").asText(),
                                    "link", res.get("link").asText()
                                ));
                            }
                        }
                        return Map.of("podTitle", podTitle, "resources", resources);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON for schedule ID '{}': {}. Raw data: {}", schedule.getId(), e.getMessage(), schedule.getDayWiseData());
            return Map.of("error", "Error parsing JSON data.", "scheduleId", schedule.getId());
        }
        return Map.of("message", "Pod not found.");
    }
}
