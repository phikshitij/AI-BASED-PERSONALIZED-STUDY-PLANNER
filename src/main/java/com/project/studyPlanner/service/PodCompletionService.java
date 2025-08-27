package com.project.studyPlanner.service;

import com.project.studyPlanner.repository.PodCompletionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PodCompletionService {

    @Autowired
    private PodCompletionRepository podCompletionRepository;

    // ✅ Fetch pod completion data for user "01"
    public Map<String, Integer> fetchPodCompletionData() {
        List<Map<String, Object>> podCompletionList = podCompletionRepository.findPodCompletionByUserId();

        Map<String, Integer> podCompletionMap = new LinkedHashMap<>();
        for (Map<String, Object> entry : podCompletionList) {
            String subject = entry.get("subject").toString();
            Integer completedPods = ((Number) entry.get("completedPods")).intValue();
            podCompletionMap.put(subject, completedPods);
        }
        return podCompletionMap;
    }
}
