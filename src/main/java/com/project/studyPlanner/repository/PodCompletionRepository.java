package com.project.studyPlanner.repository;

import com.project.studyPlanner.models.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface PodCompletionRepository extends JpaRepository<Progress, Long> {

    // ✅ Query to count completed pods per subject for user "01"
    @Query("SELECT p.subject AS subject, COUNT(p.podTitle) AS completedPods FROM Progress p WHERE p.userId = '01' GROUP BY p.subject")
    List<Map<String, Object>> findPodCompletionByUserId();
}
