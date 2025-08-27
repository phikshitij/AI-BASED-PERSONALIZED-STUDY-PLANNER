package com.project.studyPlanner.service;

import com.project.studyPlanner.models.OnboardingData;
import com.project.studyPlanner.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OnboardingService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    public OnboardingData saveOrUpdateOnboardingData(OnboardingData onboardingData) {
        // Check if data exists for the user
        Optional<OnboardingData> existingData = onboardingRepository.findByUserId(onboardingData.getUserId());
        
        if (existingData.isPresent()) {
            // Update existing data
            OnboardingData existing = existingData.get();
            existing.setAcademicLevel(onboardingData.getAcademicLevel());
            existing.setDomain(onboardingData.getDomain());
            existing.setBranch(onboardingData.getBranch());
            existing.setYear(onboardingData.getYear());
            existing.setUniversity(onboardingData.getUniversity());
            return onboardingRepository.save(existing);
        }
        
        // Create new data if none exists
        return onboardingRepository.save(onboardingData);
    }

    public OnboardingData getOnboardingDataByUserId(String userId) {
        return onboardingRepository.findByUserId(userId).orElse(null);
    }
}
