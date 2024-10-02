package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.WorkScheduleSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WorkScheduleSummaryRepository extends JpaRepository<WorkScheduleSummary, Long> {
}
