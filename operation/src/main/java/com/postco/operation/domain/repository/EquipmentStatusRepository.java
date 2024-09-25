package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.coil.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatus, Long> {
    Optional<EquipmentStatus> findByEquipmentId(Long equipmentId);
}
