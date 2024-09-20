package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.SCHHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SCHHistoryRepository extends JpaRepository<SCHHistory, Long> {
}
