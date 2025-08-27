package com.project.studyPlanner.service;

import com.project.studyPlanner.models.Progress;
import com.project.studyPlanner.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    public void markPodAsCompleted(String userId, String subject, String podTitle) {
        Progress progress = new Progress(userId, subject, podTitle, LocalDateTime.now());
        progressRepository.save(progress);
    }

    public List<Progress> getUserProgress(String userId) {
        return progressRepository.findByUserId(userId);
    }
}

