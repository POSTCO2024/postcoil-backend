package com.postco.control.domain.repository;

import com.postco.control.domain.ErrorCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorCriteriaDetailRepository extends JpaRepository<ErrorCriteria, Long> {
}
