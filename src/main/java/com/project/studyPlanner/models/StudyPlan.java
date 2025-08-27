package com.project.studyPlanner.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.ZonedDateTime;

@Entity
@Table(name = "study_plans")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ✅ Prevents serialization issues
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // ✅ Stores the user ID

    private String subject;
    private int hoursPerDay;
    private int duration;
    private String learningStyle;
    private String module; // NEW FIELD

    @Lob
    @Column(columnDefinition = "TEXT")
    private String studyData; 

    @JsonDeserialize(using = ZonedDateTimeDeserializer.class) // ✅ Custom deserializer
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") 
    private ZonedDateTime createdAt; 

    public StudyPlan() {}

    public StudyPlan(String userId, String subject, int hoursPerDay, int duration, String learningStyle, String studyData, String module) {
        this.userId = userId;
        this.subject = subject;
        this.hoursPerDay = hoursPerDay;
        this.duration = duration;
        this.learningStyle = learningStyle;
        this.studyData = studyData;
        this.module = module;
        this.createdAt = ZonedDateTime.now();
    }

    // ✅ Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; } 

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public int getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(int hoursPerDay) { this.hoursPerDay = hoursPerDay; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getLearningStyle() { return learningStyle; }
    public void setLearningStyle(String learningStyle) { this.learningStyle = learningStyle; }

    public String getStudyData() { return studyData; }
    public void setStudyData(String studyData) { this.studyData = studyData; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
