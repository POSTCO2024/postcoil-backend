package com.postco.control.domain.repository;

import com.postco.control.domain.TargetMaterial;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TargetMaterialRepository extends JpaRepository<TargetMaterial, Long> {
    List<TargetMaterial> findByIsError(String isError);  // To do: 제거

//    List<TargetMaterial> findByIsErrorAndCriteria(String y, String curProcCode);

    @Modifying
    @Transactional
    @Query("UPDATE TargetMaterial tm SET tm.isError = 'N' WHERE tm.materialId IN :errorMaterialIds")
    int updateisError(@Param("errorMaterialIds") List<Long> errorMaterialIds);
}

