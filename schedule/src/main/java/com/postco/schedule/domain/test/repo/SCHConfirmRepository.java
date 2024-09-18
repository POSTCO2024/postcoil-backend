package com.postco.schedule.domain.test.repo;

import com.postco.schedule.domain.test.SCHConfirm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SCHConfirmRepository extends JpaRepository<SCHConfirm, Long> {
    @Query("SELECT c FROM SCHConfirm c JOIN FETCH c.materials WHERE c.id = :scheduleId")
    Optional<SCHConfirm> findWithMaterialsById(@Param("scheduleId") Long scheduleId);
}
