package com.postco.operation.domain.repository;

import com.postco.operation.domain.Materials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialsRepository extends JpaRepository<Materials, Long> {
}
