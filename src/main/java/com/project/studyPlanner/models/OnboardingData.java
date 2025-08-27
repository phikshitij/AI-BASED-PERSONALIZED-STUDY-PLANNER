package com.project.studyPlanner.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "onboarding_data")
public class OnboardingData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "academic_level", nullable = false)
    private String academicLevel;

    @Column(name = "domain", nullable = false)
    private String domain;

    @Column(name = "branch")
    private String branch;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "university", nullable = false)
    private String university;

    // Explicit setter for userId
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
