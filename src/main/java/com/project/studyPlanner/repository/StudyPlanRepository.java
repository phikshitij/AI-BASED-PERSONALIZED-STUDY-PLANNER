package com.project.studyPlanner.repository;

import com.project.studyPlanner.models.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    // Deprecated: Use findAllByUserIdAndSubject instead
// Optional<StudyPlan> findByUserIdAndSubject(String userId, String subject);
    // Deprecated: Use findAllBySubject instead
// Optional<StudyPlan> findBySubject(String subject);
    // Deprecated: Use findAllByUserIdAndSubject instead
// Optional<StudyPlan> findByUserId(String userId);

    // New: Multi-result queries
    List<StudyPlan> findAllByUserIdAndSubject(String userId, String subject);
    /**
 * Returns all study plans for the given subject, regardless of userId (even if userId is null).
 */
List<StudyPlan> findAllBySubject(String subject);

    // Deprecated: Use list-based queries instead
// Optional<StudyPlan> findById(Long id);

    // NEW: Find by user, subject, and module
    // Deprecated: Use findAllByUserIdAndSubjectAndModule instead
// Optional<StudyPlan> findByUserIdAndSubjectAndModule(String userId, String subject, String module);
    List<StudyPlan> findAllByUserIdAndSubjectAndModule(String userId, String subject, String module);
	
}
