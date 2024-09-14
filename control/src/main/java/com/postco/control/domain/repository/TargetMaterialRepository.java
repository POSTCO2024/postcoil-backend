package com.postco.control.domain.repository;

import com.postco.control.domain.TargetMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TargetMaterialRepository extends JpaRepository<TargetMaterial, Long> {
    Optional<TargetMaterial> findByMaterialIdAndMaterialNo(Long materialId, String materialNo);


//    List<TargetMaterial> findByIsErrorAndCriteria(String isError, String criteria);
}

