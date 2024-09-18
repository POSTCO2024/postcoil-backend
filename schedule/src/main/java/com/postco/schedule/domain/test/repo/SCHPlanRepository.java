package com.postco.schedule.domain.test.repo;

import com.postco.schedule.domain.test.SCHPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SCHPlanRepository extends JpaRepository<SCHPlan, Long> {
    @Query("SELECT MAX(s.id) FROM SCHPlan s")
    Long findLastSavedId();
}
