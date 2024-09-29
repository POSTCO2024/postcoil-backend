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

    // materialId로 작업대상재 조회
    Optional<TargetMaterial> findByMaterialId(Long materialId);

    // 작업 대상재 조회
    List<TargetMaterial> findByIsError(String isError);

    // 페이징된 정상재
//    Page<TargetMaterial> findByIsErrorIsN(Pageable pageable);

    // 에러패스
    @Modifying
    @Transactional
    @Query("UPDATE TargetMaterial tm SET tm.isError = 'N' WHERE tm.id IN :errorMaterialIds")
    int updateisError(@Param("errorMaterialIds") List<Long> errorMaterialIds);


    // 생산 기한일
    @Query("SELECT tm.materialNo, tm.dueDate FROM TargetMaterial tm WHERE tm.id IN :materialIds AND tm.isError = 'N' ORDER BY tm.dueDate ASC")
    List<Object[]> findMaterialNoAndDueDateByMaterialIds(@Param("materialIds") List<Long> materialIds);

    // 에러재/정상재 비율
    long countByMaterialIdInAndIsError(List<Long> materialIds, String isError);

    // 고객사
    @Query("SELECT tm.customerName, COUNT(tm) FROM TargetMaterial tm WHERE tm.id IN :materialIds AND tm.isError = 'N' GROUP BY tm.customerName")
    List<Object[]> countByMaterialIdIn(@Param("materialIds") List<Long> materialIds);

    // 품종 & 재료(폭, 두께)
    @Query("SELECT tm.id FROM TargetMaterial tm WHERE tm.isError = 'N'")
    List<Long> findNormalMaterialIds();

    // 롤 단위
    @Query("SELECT tm.rollUnitName, COUNT(tm) FROM TargetMaterial tm WHERE tm.id IN :materialIds AND tm.isError = 'N' GROUP BY tm.rollUnitName")
    List<Object[]> countByRollUnitName(@Param("materialIds") List<Long> materialIds);
}

