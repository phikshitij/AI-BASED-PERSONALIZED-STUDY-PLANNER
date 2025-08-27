package com.project.studyPlanner.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress_tracking")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String subject;
    private String podTitle;
    private LocalDateTime completedAt;

    public Progress() {}

    public Progress(String userId, String subject, String podTitle, LocalDateTime completedAt) {
        this.userId = userId;
        this.subject = subject;
        this.podTitle = podTitle;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getSubject() { return subject; }
    public String getPodTitle() { return podTitle; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setPodTitle(String podTitle) { this.podTitle = podTitle; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

