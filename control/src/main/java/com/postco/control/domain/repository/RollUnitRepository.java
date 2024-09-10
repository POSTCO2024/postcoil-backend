package com.postco.control.domain.repository;

import com.postco.control.domain.RollUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RollUnitRepository extends JpaRepository<RollUnit, Long> {
}
