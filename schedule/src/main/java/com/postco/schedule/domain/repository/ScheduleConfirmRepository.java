package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.ScheduleConfirm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleConfirmRepository extends JpaRepository<ScheduleConfirm, Long> {
    List<ScheduleConfirm> findByProcessCode(String processCode);

    @Query("SELECT sr FROM ScheduleConfirm sr WHERE FUNCTION('SUBSTRING', sr.confirmDate, 1, 6) BETWEEN :startDate AND :endDate")
    List<ScheduleConfirm> findByConfirmDateBetween(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
