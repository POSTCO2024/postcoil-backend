package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.WorkInstructionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkInstructionItem, Long> {
    @Query("SELECT i FROM WorkInstructionItem i JOIN FETCH i.workInstruction WHERE i.id = :id")
    Optional<WorkInstructionItem> findByIdWithWorkInstruction(@Param("id") Long id);
}
