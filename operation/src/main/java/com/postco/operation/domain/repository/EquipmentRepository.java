package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.coil.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
