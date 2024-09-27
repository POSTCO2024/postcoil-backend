package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.coil.ColdStandardReduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ColdStandardReductionRepository extends JpaRepository<ColdStandardReduction, Long> {
    Optional<ColdStandardReduction> findByCoilTypeCodeAndProcess(String coilTypeCode, String currProc);
}

