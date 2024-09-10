package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.ScheduleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleResultRepository extends JpaRepository<ScheduleResult, Long> {
    List<ScheduleResult> findByCurProc(String curProc);

    @Query("SELECT sr FROM ScheduleResult sr WHERE FUNCTION('SUBSTRING', sr.planDateTime, 1, 6) BETWEEN :startDate AND :endDate")
    List<ScheduleResult> findByPlanDateTimeBetween(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
