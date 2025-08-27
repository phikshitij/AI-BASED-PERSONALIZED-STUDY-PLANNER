package com.project.studyPlanner.learningpod;

import com.project.studyPlanner.learningpod.LearningPod;
import com.project.studyPlanner.schedule.StudySchedule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LearningPodServiceTest {
    private LearningPodService learningPodService;

    @BeforeEach
    public void setup() {
        learningPodService = new LearningPodService();
    }

    @Test
    public void testGenerateLearningPods_Basic() {
        // Arrange: Create a simple study schedule
        List<StudySchedule> schedule = new ArrayList<>();
        StudySchedule day1 = new StudySchedule("Day 1", "Math - Topic 1", 2, Arrays.asList("Key1", "Key2", "Key3", "Key4"), Arrays.asList());
        schedule.add(day1);

        // Act: Generate pods for 2 hours per day
        List<LearningPod> pods = learningPodService.generateLearningPods(schedule, 2);

        // Assert: Should create at least one pod
        Assertions.assertFalse(pods.isEmpty(), "Pods should be generated");
        Assertions.assertEquals(day1.getDay(), pods.get(0).getDay(), "Pod day should match schedule day");
        Assertions.assertEquals(day1.getTopic(), pods.get(0).getTopic(), "Pod topic should match schedule topic");
    }

    @Test
    public void testGenerateLearningPods_ZeroHours() {
        // Arrange: Create a simple study schedule
        List<StudySchedule> schedule = new ArrayList<>();
        StudySchedule day1 = new StudySchedule("Day 1", "Math - Topic 1", 0, Arrays.asList("K1"), Arrays.asList());
        schedule.add(day1);

        // Act: Generate pods for 0 hours per day
        List<LearningPod> pods = learningPodService.generateLearningPods(schedule, 0);

        // Assert: Should not create any pods
        Assertions.assertTrue(pods.isEmpty(), "No pods should be generated for 0 hours");
    }

    @Test
    public void testGenerateLearningPods_EmptySchedule() {
        // Arrange: Empty schedule
        List<StudySchedule> schedule = new ArrayList<>();

        // Act
        List<LearningPod> pods = learningPodService.generateLearningPods(schedule, 2);

        // Assert
        Assertions.assertTrue(pods.isEmpty(), "No pods should be generated for empty schedule");
    }
}
