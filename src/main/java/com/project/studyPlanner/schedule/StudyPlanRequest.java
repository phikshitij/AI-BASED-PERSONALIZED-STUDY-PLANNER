package com.project.studyPlanner.schedule;

public class StudyPlanRequest {
    private String subject;
    private int hoursPerDay;
    private int duration;
    private String learningStyle;

    // Constructor
    public StudyPlanRequest(String subject, int hoursPerDay, int duration, String learningStyle) {
        this.subject = subject;
        this.hoursPerDay = hoursPerDay;
        this.duration = duration;
        this.learningStyle = learningStyle;
    }

    // Getters
    public String getSubject() { return subject; }
    public int getHoursPerDay() { return hoursPerDay; }
    public int getDuration() { return duration; }
    public String getLearningStyle() { return learningStyle; }

    // Setters
    public void setSubject(String subject) { this.subject = subject; }
    public void setHoursPerDay(int hoursPerDay) { this.hoursPerDay = hoursPerDay; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setLearningStyle(String learningStyle) { this.learningStyle = learningStyle; }
}
