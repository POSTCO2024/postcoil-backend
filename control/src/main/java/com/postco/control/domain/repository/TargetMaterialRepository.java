package com.postco.control.domain.repository;

import com.postco.control.domain.TargetMaterial;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.core.dto.TargetMaterialDTO;
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

    // 정상재만 가져오기
    List<TargetMaterial> findByIsError(String isError);

    // 에러패스
    @Modifying
    @Transactional
    @Query("UPDATE TargetMaterial tm SET tm.isError = 'N' WHERE tm.materialId IN :errorMaterialIds")
    int updateisError(@Param("errorMaterialIds") List<Long> errorMaterialIds);


    // 생산 기한일
    @Query("SELECT tm.materialNo, tm.dueDate FROM TargetMaterial tm ORDER BY tm.dueDate ASC")
    List<Object[]> findMaterialNoAndDueDate();

    // 에러재/정상재 비율
    long countByIsError(String isError);


    // 고객사
    @Query("SELECT t.customerName, COUNT(t) FROM TargetMaterial t GROUP BY t.customerName")
    List<Object[]> countByCustomerName();

}

