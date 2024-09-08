package com.postco.control.domain.repository;

import com.postco.control.domain.ExtractionCriteriaMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtractionCriteriaRepository extends JpaRepository<ExtractionCriteriaMapper, Long> {
    Optional<ExtractionCriteriaMapper> findByProcessCode(String processCode);
}
