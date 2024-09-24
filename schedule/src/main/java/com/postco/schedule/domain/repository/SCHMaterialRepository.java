package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.SCHMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SCHMaterialRepository extends JpaRepository<SCHMaterial, Long> {
    @Query("SELECT m FROM SCHMaterial m WHERE m.schPlan.id = :planId AND m.id IN :materialIds")
    List<SCHMaterial> findBySchPlanIdAndMaterialIds(@Param("planId") Long planId, @Param("materialIds") List<Long> materialIds);

    List<SCHMaterial> findBySchConfirmId(Long confirmId);

    // 추가 기능 - SohyunAhn
    List<SCHMaterial> findBySchPlanId(Long planId);
    List<SCHMaterial> findByIdIn(List<Long> ids);
    List<SCHMaterial> findByCurrProc(String currProc);
    List<SCHMaterial> findAllById(Iterable<Long> id);
    List<SCHMaterial> findBySchPlanIsNullAndSchConfirmIsNullAndCurrProc(String currProc);
    List<SCHMaterial> findBySchPlanIsNotNullAndSchConfirmIsNullAndCurrProc(String currProc);

}
