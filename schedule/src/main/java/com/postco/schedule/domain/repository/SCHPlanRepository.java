package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.SCHPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SCHPlanRepository extends JpaRepository<SCHPlan, Long> {
    @Query("SELECT MAX(s.id) FROM SCHPlan s")
    Long findLastSavedId();

    Optional<SCHPlan> findById(Long id);
    List<SCHPlan> findByProcess(String processCode);

}
