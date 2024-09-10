package com.postco.schedule.domain.repository;

import com.postco.schedule.domain.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {
    List<Priority> findByProcessCodeAndRollUnit(String processCode, String rollUnit);
    List<Priority> findByProcessCode(String processCode);
}
