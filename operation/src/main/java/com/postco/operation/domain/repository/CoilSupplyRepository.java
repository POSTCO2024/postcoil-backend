package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.CoilSupply;
import com.postco.operation.domain.entity.WorkInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoilSupplyRepository extends JpaRepository<CoilSupply, Long>, JpaSpecificationExecutor<CoilSupply> {
    Optional<CoilSupply> findByWorkInstruction(WorkInstruction workInstruction);

    Optional<CoilSupply> findByWorkInstructionId(@Param("workInstructionId") Long workInstructionId);

    @Query("SELECT cs FROM CoilSupply cs JOIN FETCH cs.workInstruction wi WHERE wi.id = :workInstructionId")
    Optional<CoilSupply> findByWorkInstructionIdWithWorkInstruction(@Param("workInstructionId") Long workInstructionId);
}

