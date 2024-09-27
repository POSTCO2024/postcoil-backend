package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.coil.Equipment;
import com.postco.operation.domain.entity.coil.EquipmentStatus;
import com.postco.operation.domain.repository.EquipmentRepository;
import com.postco.operation.domain.repository.EquipmentStatusRepository;
import com.postco.operation.service.EquipmentStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquipmentStatusServiceImpl implements EquipmentStatusService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;


    @Override
    @Transactional
    public boolean startEquipment(String process) {
        try {
            Equipment equipment = equipmentRepository.findByProcess(process)
                    .orElseThrow(() -> new EntityNotFoundException("Equipment not Found"));

            EquipmentStatus newStatus = new EquipmentStatus();
            newStatus.setEquipment(equipment);

            newStatus.startEquipment();

            equipmentStatusRepository.save(newStatus);

            return true;
        }catch (Exception e) {
            log.info("설비 상태 업데이트 중 오류 발생 : {} ", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean stopEquipment(Long equipmentStatusId) {
        try {
            EquipmentStatus status = equipmentStatusRepository.findById(equipmentStatusId)
                    .orElseThrow(() -> new EntityNotFoundException("EquipmentStatus not found"));

            status.stopEquipment();
            equipmentStatusRepository.save(status);

            log.info("설비 가동 중지. 설비 상태 ID: {}", equipmentStatusId);
            return true;
        } catch (Exception e) {
            log.error("설비 중지 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean brokenEquipment(Long equipmentStatusId) {
        try {
            EquipmentStatus status = equipmentStatusRepository.findById(equipmentStatusId)
                    .orElseThrow(() -> new EntityNotFoundException("EquipmentStatus not found"));

            status.brokenEquipment();
            equipmentStatusRepository.save(status);

            log.info("설비 고장 처리. 설비 상태 ID: {}", equipmentStatusId);
            return true;
        } catch (Exception e) {
            log.error("설비 고장 처리 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getCurrentOperationalTime(Long equipmentStatusId) {
        try {
            EquipmentStatus status = equipmentStatusRepository.findById(equipmentStatusId)
                    .orElseThrow(() -> new EntityNotFoundException("EquipmentStatus not found"));

            long operationalTime = status.calculateCurrentOperationalTime();
            log.info("현재 가동 시간 조회. 설비 상태 ID: {}, 가동 시간: {} 초", equipmentStatusId, operationalTime);

            return operationalTime;
        } catch (Exception e) {
            log.error("가동 시간 조회 중 오류 발생: {}", e.getMessage());
            return -1;
        }
    }
}
