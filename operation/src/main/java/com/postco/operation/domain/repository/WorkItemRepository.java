package com.postco.operation.domain.repository;

import com.postco.operation.domain.WorkInstructionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkInstructionItem, Long> {
}
