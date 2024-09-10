package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.Materials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Materials, Long> {
}
