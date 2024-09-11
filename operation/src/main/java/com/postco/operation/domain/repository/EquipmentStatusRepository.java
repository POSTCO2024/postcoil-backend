package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.coil.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatus, Long> {
}
