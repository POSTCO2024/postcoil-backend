package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.entity.WorkStatus;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkInstructionItem, Long> {
    @Query("SELECT wi FROM WorkInstructionItem wi JOIN FETCH wi.workInstruction w JOIN FETCH w.items WHERE wi.id = :itemId")
    Optional<WorkInstructionItem> findByIdWithWorkInstruction(@Param("itemId") Long itemId);
    Optional<List<WorkInstructionItem>> findAllByWorkItemStatus(WorkStatus workStatus);

    @Query("SELECT wii FROM WorkInstructionItem wii WHERE wii.workInstruction.id = :workInstructionId ORDER BY wii.sequence")
    List<WorkInstructionItem> findAllItemsByWorkInstructionId(@Param("workInstructionId") Long workInstructionId);
}
