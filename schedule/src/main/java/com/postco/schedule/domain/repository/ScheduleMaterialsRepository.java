package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.ScheduleMaterials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleMaterialsRepository extends JpaRepository<ScheduleMaterials, Long> {
    List<ScheduleMaterials> findAllById(Iterable<Long> ids);
    List<ScheduleMaterials> findAllByCurProcCode(String processCode);
}
