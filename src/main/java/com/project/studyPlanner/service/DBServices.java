package com.project.studyPlanner.service;

import com.project.studyPlanner.models.StudyPlan; 
import com.project.studyPlanner.repository.StudyPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DBServices {

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    // Save a new study plan
    public StudyPlan saveStudyPlan(StudyPlan studyPlan) {
        return studyPlanRepository.save(studyPlan);
    }

    // Get all study plans
    public List<StudyPlan> getAllStudyPlans() {
        return studyPlanRepository.findAll();
    }

    // Get a study plan by ID
    public Optional<StudyPlan> getStudyPlanById(Long id) {
        return studyPlanRepository.findById(id);
    }

    // Delete a study plan by ID
    public void deleteStudyPlan(Long id) {
        studyPlanRepository.deleteById(id);
    }
    /**
     * Returns all study plans for a subject.
     */
    public List<StudyPlan> getStudyPlansBySubject(String subject) {
        return studyPlanRepository.findAllBySubject(subject);
    }

    /**
     * @deprecated Use getStudyPlansBySubject for multi-result cases.
     */
    // New method for multi-user support
    /**
     * Returns all study plans for a user and subject.
     */
    public List<StudyPlan> getStudyPlansByUserIdAndSubject(String userId, String subject) {
        return studyPlanRepository.findAllByUserIdAndSubject(userId, subject);
    }

    /**
     * Use getStudyPlansByUserIdAndSubject for multi-result cases.
     * This method is deprecated and removed to avoid NonUniqueResultException.
     */
    // public Optional<StudyPlan> getStudyPlanByUserIdAndSubject(String userId, String subject) {
    //     return studyPlanRepository.findByUserIdAndSubject(userId, subject);
    // }


}
