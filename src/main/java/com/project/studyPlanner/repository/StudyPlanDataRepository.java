package com.project.studyPlanner.repository;

import com.project.studyPlanner.models.StudyPlanData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudyPlanDataRepository extends JpaRepository<StudyPlanData, Long> {
    Optional<StudyPlanData> findByUserId(String userId);
}
