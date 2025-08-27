package com.project.studyPlanner.repository;

import com.project.studyPlanner.models.DayWiseSchedule;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayWiseScheduleRepository extends JpaRepository<DayWiseSchedule, Long> {
	
    // ✅ Custom query method to find a schedule by userId
    
    List<DayWiseSchedule> findAllByUserIdAndSubject(String userId, String subject);
    
    // Fetch all schedules for a subject (ignore userId)
    List<DayWiseSchedule> findAllBySubject(String subject);
    
    // ❌ Fix: Change return type from Optional to List
    List<DayWiseSchedule> findByUserId(String userId);

}
