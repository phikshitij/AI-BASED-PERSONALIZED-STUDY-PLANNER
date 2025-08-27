package com.project.studyPlanner.service;

import com.project.studyPlanner.models.StudyPlanData;
import com.project.studyPlanner.repository.StudyPlanDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudyPlanDataService {

    @Autowired
    private StudyPlanDataRepository studyPlanDataRepository;

    public Optional<StudyPlanData> getUserStudyPlanData(String userId) {
        return studyPlanDataRepository.findByUserId(userId);
    }
}
