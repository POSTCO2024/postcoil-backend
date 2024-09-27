package com.postco.operation.domain.repository;

import com.postco.operation.domain.entity.coil.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByProcess(String process);
}
