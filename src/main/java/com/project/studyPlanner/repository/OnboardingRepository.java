package com.project.studyPlanner.repository;

import com.project.studyPlanner.models.OnboardingData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OnboardingRepository extends JpaRepository<OnboardingData, Long> {
    Optional<OnboardingData> findByUserId(String userId);
}
