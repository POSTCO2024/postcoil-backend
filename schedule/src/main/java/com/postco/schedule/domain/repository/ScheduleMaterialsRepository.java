package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.ScheduleMaterials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleMaterialsRepository extends JpaRepository<ScheduleMaterials, Long> {
    List<ScheduleMaterials> findAllById(Iterable<Long> ids);
    List<ScheduleMaterials> findAllByCurrProc(String currProc);
    List<ScheduleMaterials> findAllByScheduleId(Long scheduleId);
    List<ScheduleMaterials> findAllByScheduleNo(String scheduleNo);
    List<ScheduleMaterials> findByIdIn(List<Long> ids);
    List<ScheduleMaterials> findByScheduleIdIsNullAndCurrProc(String processCode);

}
