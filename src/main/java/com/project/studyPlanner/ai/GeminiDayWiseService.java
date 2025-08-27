package com.project.studyPlanner.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.studyPlanner.config.GeminiConfig;
import com.project.studyPlanner.models.StudyPlan;
import com.project.studyPlanner.models.DayWiseSchedule;
import com.project.studyPlanner.repository.DayWiseScheduleRepository;
import com.project.studyPlanner.repository.StudyPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class GeminiDayWiseService {
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final DayWiseScheduleRepository dayWiseScheduleRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GeminiDayWiseService.class);

    // ✅ Constructor
    public GeminiDayWiseService(GeminiConfig geminiConfig, DayWiseScheduleRepository dayWiseScheduleRepository, StudyPlanRepository studyPlanRepository) {
        this.geminiConfig = geminiConfig;
        this.dayWiseScheduleRepository = dayWiseScheduleRepository;
        this.studyPlanRepository = studyPlanRepository;
    }

    /**
     * ✅ Replaces placeholders with actual links.
     */
    private String replacePlaceholdersWithRealLinks(String responseText) {
        // Define real resource mappings with actual, working URLs
        Map<String, String> resourceMappings = new HashMap<>();
        
        // YouTube videos for Automata Theory
        resourceMappings.put("REPLACE_WITH_YOUTUBE_RESOURCE_LINK", "https://www.youtube.com/watch?v=58N2N7zJGrQ"); // Real Automata Theory video
        resourceMappings.put("YOUTUBE", "https://www.youtube.com/watch?v=58N2N7zJGrQ");
        
        // Shaalaa resources
        resourceMappings.put("REPLACE_WITH_SHAALAA_RESOURCE_LINK", "https://www.shaalaa.com/question-bank-solutions/theory-computation_357");
        resourceMappings.put("SHAALAA", "https://www.shaalaa.com/question-bank-solutions/theory-computation_357");
        
        // University resources
        resourceMappings.put("REPLACE_WITH_MUMBAI_UNIVERSITY_RESOURCE_LINK", "https://old.mu.ac.in/wp-content/uploads/2016/06/4.11-T.Y.B.Sc_.-CS-Sem-V-Theory-of-Computation.pdf");
        resourceMappings.put("MUMBAI_UNIVERSITY", "https://old.mu.ac.in/wp-content/uploads/2016/06/4.11-T.Y.B.Sc_.-CS-Sem-V-Theory-of-Computation.pdf");

        // Replace all placeholders with actual links
        for (Map.Entry<String, String> entry : resourceMappings.entrySet()) {
            responseText = responseText.replaceAll(
                "(?i)" + Pattern.quote(entry.getKey()) + "(_\\d+)?",
                entry.getValue()
            );
        }

        // Clean up any remaining placeholder-like text
        responseText = responseText.replaceAll("\\(REPLACE_WITH_[^)]+\\)", "(https://www.youtube.com/watch?v=58N2N7zJGrQ)");
        
        return responseText;
    }

    /**
     * ✅ Generates and stores a day-wise schedule.
     */
    public DayWiseSchedule generateAndStoreDayWiseSchedule(String subject, String userId) {
        // 🛠 Check if schedule already exists
        List<DayWiseSchedule> existingSchedules = dayWiseScheduleRepository.findAllByUserIdAndSubject(userId, subject);
        if (!existingSchedules.isEmpty()) {
            logger.info("Schedule already exists for subject: {}", subject);
            return existingSchedules.get(0);
        }

        // Fetch all study plans for userId and subject
        // Always fetch study plans by subject only, ignore userId
        List<StudyPlan> studyPlans = studyPlanRepository.findAllBySubject(subject);
        logger.info("Study plans found for subject '{}': {}", subject, studyPlans);
        if (studyPlans == null || studyPlans.isEmpty()) {
            logger.error("No study plans found for subject '{}'", subject);
            return null;
        }
        StudyPlan studyPlan = studyPlans.get(0);

        try {
            logger.info("📡 Sending request to Gemini AI for subject: {}", subject);
        
            // AI Prompt with STRICT Link Requirement
            // ✅ AI Prompt with STRICT Link Requirement
            String prompt = """
                "Convert the given study schedule into JSON format.  
                - **Include ONLY real, publicly available links** (YouTube, university PDFs, educational sites).  
                - **Do NOT generate placeholder links** (e.g., REPLACE_WITH_LINK).  
                - **Ensure all links exist and are accessible.**  
                - **Each pod must have at least 3 quizzes related to its resources.**  
                  
                ### JSON Output Format:
                {
                  "Day 1": {
                    "pods": [
                      {
                        "title": "Pod 1 Title",
                        "key_concepts": ["Concept 1", "Concept 2"],
                        "resources": [
                          {"type": "video", "title": "Video 1", "link": "https://example.com"}
                        ],
                        "quiz": [
                          {"question": "Q1?", "options": ["A", "B", "C", "D"], "answer": "A"}
                        ]
                      }
                    ]
                  }
                }
                - **Return JSON only, no extra text.**"
                """;

            // ✅ Prepare API Request
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "role", "user",
                        "parts", List.of(
                            Map.of("text", prompt + "\n\n" + studyPlan.getStudyData())
                        )
                    )
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            String geminiApiUrl = geminiConfig.getGeminiApiUrl() + "?key=" + geminiConfig.getGeminiApiKey();

            // ✅ Make API call
            ResponseEntity<Map> responseEntity = restTemplate.exchange(geminiApiUrl, HttpMethod.POST, requestEntity, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            // ✅ Log full response for debugging
            logger.debug("📝 Full API Response: {}", response);

            // ✅ Validate API Response
            if (response == null || response.isEmpty()) {
                logger.error("⚠️ Empty response received from Gemini AI.");
                return null;
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                logger.error("❌ No 'candidates' found in AI response. Full response: {}", response);
                return null;
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            if (!firstCandidate.containsKey("content")) {
                logger.error("❌ No 'content' found in response.");
                return null;
            }

            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            if (!content.containsKey("parts")) {
                logger.error("❌ No 'parts' found in response.");
                return null;
            }

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                logger.error("❌ 'parts' list is empty.");
                return null;
            }

            String responseText = (String) parts.get(0).get("text");
            if (responseText == null || responseText.trim().isEmpty()) {
                logger.error("❌ AI response text is empty.");
                return null;
            }

            // ✅ Clean AI Response (Remove ```json formatting)
            responseText = responseText.replaceAll("```json", "").replaceAll("```", "").trim();

            // ✅ Replace placeholders
            responseText = replacePlaceholdersWithRealLinks(responseText);

            logger.info("✅ Final AI Response (Processed): {}", responseText);

            // ✅ Save the generated schedule
            DayWiseSchedule newSchedule = new DayWiseSchedule();
            newSchedule.setUserId(userId);
            newSchedule.setSubject(subject);
            newSchedule.setStartDate(LocalDate.now());
            newSchedule.setDayWiseData(responseText);

            DayWiseSchedule savedSchedule = dayWiseScheduleRepository.save(newSchedule);
            logger.info("✅ Schedule saved successfully for subject '{}'", subject);

            return savedSchedule;

        } catch (Exception e) {
            logger.error("❌ Unexpected Error while generating/storing day-wise schedule for subject '{}', user '{}': {}", subject, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate schedule: " + e.getMessage(), e);
        }

        // This should never be reached
        // return null;
    }
}
