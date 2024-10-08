package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.WorkInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkInstructionRepository extends JpaRepository<WorkInstruction, Long>, JpaSpecificationExecutor<WorkInstruction> {
    @Query("SELECT MAX(w.id) FROM WorkInstruction w")
    Long findLastSavedId();

    @Query("SELECT DISTINCT w FROM WorkInstruction w LEFT JOIN FETCH w.items  i LEFT JOIN FETCH i.material WHERE w.process = :process AND w.rollUnit = :rollUnit")
    List<WorkInstruction> findByProcessAndRollUnit(String process, String rollUnit);

    @Query("SELECT w FROM WorkInstruction w LEFT JOIN FETCH w.items WHERE w.id = :id")
    Optional<WorkInstruction> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT w FROM WorkInstruction w LEFT JOIN FETCH w.items i LEFT JOIN FETCH i.material WHERE w.process = :process and w.workStatus != 'COMPLETED'")
    List<WorkInstruction> findUncompletedWithItems(String process);
    @Query("SELECT DISTINCT w FROM WorkInstruction w  WHERE w.workStatus = 'PENDING'")
    List<WorkInstruction> findUncompletedWithItemsForSimulation();
//    @Query("SELECT DISTINCT w FROM WorkInstruction w LEFT JOIN FETCH w.items i LEFT JOIN FETCH i.material WHERE w.workStatus != 'COMPLETED'")
//    List<WorkInstruction> findUncompletedWithItemsForSimulation();

    @Query("SELECT DISTINCT w FROM WorkInstruction w LEFT JOIN FETCH w.items i LEFT JOIN FETCH i.material " +
            "WHERE w.workStatus = 'COMPLETED' " +
            "AND w.process = :process " +
            "AND w.endTime >= :startDate " +
            "AND w.endTime <= :endDate")
    List<WorkInstruction> findCompletedWithItems(@Param("process") String process,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT w FROM WorkInstruction w LEFT JOIN FETCH w.items i LEFT JOIN FETCH i.material WHERE w.workStatus = 'IN_PROGRESS'")
    List<WorkInstruction> findInProgressWorkInstructions();
}
