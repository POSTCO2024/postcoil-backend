package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.WorkInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.List;

public interface WorkInstructionRepository extends JpaRepository<WorkInstruction, Long> {
    @Query("SELECT MAX(w.id) FROM WorkInstruction w")
    Long findLastSavedId();

    @Query("SELECT DISTINCT w FROM WorkInstruction w LEFT JOIN FETCH w.items WHERE w.process = :process AND w.rollUnit = :rollUnit")
    List<WorkInstruction> findByProcessAndRollUnit(String process, String rollUnit);
}
