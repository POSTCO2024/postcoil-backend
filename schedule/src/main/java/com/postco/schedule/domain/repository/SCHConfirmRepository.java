package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.SCHConfirm;
import com.postco.schedule.domain.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SCHConfirmRepository extends JpaRepository<SCHConfirm, Long> {
    @Query("SELECT c FROM SCHConfirm c JOIN FETCH c.materials WHERE c.id = :scheduleId")
    Optional<SCHConfirm> findWithMaterialsById(@Param("scheduleId") Long scheduleId);

    List<SCHConfirm> findByWorkStatusAndProcess(WorkStatus workStatus, String process);

    List<SCHConfirm> findByWorkStatusAndProcessOrderByConfirmDateAsc(WorkStatus workStatus, String process);

    // 특정 process에 대해 startDate와 endDate 사이에 있는 레코드를 조회
    @Query("SELECT c FROM SCHConfirm c WHERE c.confirmDate BETWEEN :startDate AND :endDate AND c.process = :process")
    List<SCHConfirm> findByConfirmDateBetweenAndProcess(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("process") String process);
}
