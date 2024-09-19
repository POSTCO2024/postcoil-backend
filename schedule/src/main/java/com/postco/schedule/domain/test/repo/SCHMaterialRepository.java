package com.postco.schedule.domain.test.repo;

import com.postco.schedule.domain.test.SCHMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SCHMaterialRepository extends JpaRepository<SCHMaterial, Long> {
    @Query("SELECT m FROM SCHMaterial m WHERE m.schPlan.id = :planId AND m.id IN :materialIds")
    List<SCHMaterial> findBySchPlanIdAndMaterialIds(@Param("planId") Long planId, @Param("materialIds") List<Long> materialIds);

    List<SCHMaterial> findBySchConfirmId(Long confirmId);
}
