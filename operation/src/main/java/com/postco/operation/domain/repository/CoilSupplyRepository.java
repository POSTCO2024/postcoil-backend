package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.CoilSupply;
import com.postco.operation.domain.entity.WorkInstruction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoilSupplyRepository extends JpaRepository<CoilSupply, Long> {
    Optional<CoilSupply> findByWorkInstruction(WorkInstruction workInstruction);
    Optional<CoilSupply> findByWorkInstructionId(Long workInstructionId);
}
