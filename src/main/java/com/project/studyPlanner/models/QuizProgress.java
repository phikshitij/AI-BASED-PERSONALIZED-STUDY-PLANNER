package com.project.studyPlanner.models;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_scores") // ✅ Correct table name
public class QuizProgress {  // ✅ Keeping your existing file name

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String subject;
    private int correctAnswers;
    private int totalQuestions;

    public QuizProgress() {}

    public QuizProgress(String userId, String subject, int correctAnswers, int totalQuestions) {
        this.userId = userId;
        this.subject = subject;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getSubject() { return subject; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getTotalQuestions() { return totalQuestions; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
}
