package com.project.studyPlanner.repository;

import com.project.studyPlanner.models.QuizScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface QuizProgressRepository extends JpaRepository<QuizScore, Long> {

    // ✅ Fetch quiz scores as a percentage
    @Query("SELECT q.subject AS subject, " +
           "COALESCE((CAST(q.correctAnswers AS double) / NULLIF(q.totalQuestions, 0)) * 100, 0) AS score " +
           "FROM QuizScore q WHERE q.userId = :userId")
    List<Object[]> findQuizScoresByUserId(String userId);
}
