package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.ConstraintInsertion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstraintInsertionRepository extends JpaRepository<ConstraintInsertion, Long> {
    List<ConstraintInsertion> findByProcessCodeAndMaterialUnitCode(String processCode, String materialUnitCode);
}
