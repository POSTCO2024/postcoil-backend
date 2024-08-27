package com.postco.control.domain.repository;

import com.postco.control.domain.ErrorCriteriaMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErrorCriteriaRepository extends JpaRepository<ErrorCriteriaMapper, Long> {
    Optional<ErrorCriteriaMapper> findByProcessCode(String processCode);
}
