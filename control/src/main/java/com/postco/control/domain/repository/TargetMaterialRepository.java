package com.postco.control.domain.repository;

import com.postco.control.domain.TargetMaterial;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetMaterialRepository extends JpaRepository<TargetMaterial, Long> {
    Optional<TargetMaterial> findByMaterialIdAndMaterialNo(Long materialId, String materialNo);

    // 공정 별 작업대상재/에러재 조회
    // List<TargetMaterial> findByIsErrorAndCriteria(String isError, String criteria);

    // 에러패스
    @Modifying
    @Transactional
    @Query("UPDATE TargetMaterial tm SET tm.isError = 'N' WHERE tm.materialId IN :errorMaterialIds")
    int updateisError(@Param("errorMaterialIds") List<Long> errorMaterialIds);
}

