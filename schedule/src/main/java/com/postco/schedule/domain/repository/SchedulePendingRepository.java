package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.SchedulePending;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchedulePendingRepository extends JpaRepository<SchedulePending, Long> {
    Optional<SchedulePending> findByNo(String no);
    List<SchedulePending> findByCurProc(String curProc);
}
