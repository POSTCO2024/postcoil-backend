package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.SchedulePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulePlanRepository extends JpaRepository<SchedulePlan, Long> {
    Optional<SchedulePlan> findById(Long id);
    Optional<SchedulePlan> findByNo(String no);
    List<SchedulePlan> findByProcessCode(String processCode);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(sp.no, 8, 4) AS int)), 0) " +
            "FROM SchedulePlan sp " +
            "WHERE SUBSTRING(sp.no, 2, LENGTH(:processCode)) = :processCode " +
            "AND SUBSTRING(sp.no, LENGTH(sp.no) - LENGTH(:rollUnitName) + 1, LENGTH(:rollUnitName)) = :rollUnitName")
    Integer findMaxScheduleNoByProcessCodeAndRollUnit(@Param("processCode") String processCode, @Param("rollUnitName") String rollUnitName);
}
