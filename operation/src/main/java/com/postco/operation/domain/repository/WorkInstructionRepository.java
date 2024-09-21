package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.WorkInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface WorkInstructionRepository extends JpaRepository<WorkInstruction, Long> {
    @Query("SELECT MAX(w.id) FROM WorkInstruction w")
    Long findLastSavedId();
}
