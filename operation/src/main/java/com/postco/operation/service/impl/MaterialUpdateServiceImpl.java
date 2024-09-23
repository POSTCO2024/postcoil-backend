package com.postco.operation.service.impl;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.utils.mapper.MapperUtils;
import com.postco.operation.domain.entity.MaterialProgress;
import com.postco.operation.domain.entity.Materials;
import com.postco.operation.domain.entity.Order;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.entity.coil.ColdStandardReduction;
import com.postco.operation.domain.repository.ColdStandardReductionRepository;
import com.postco.operation.domain.repository.MaterialRepository;
import com.postco.operation.domain.repository.OrderRepository;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.infra.kafka.MaterialsProducer;
import com.postco.operation.infra.kafka.OrderProducer;
import com.postco.operation.service.MaterialUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MaterialUpdateServiceImpl implements MaterialUpdateService {
    private final MaterialRepository materialRepository;
    private final ColdStandardReductionRepository coldStandardReductionRepository;

    @Override
    @Transactional
    public boolean updateMaterialProgress(Long materialId, MaterialProgress newProgress) {
        try {
            Materials material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new EntityNotFoundException("Material not found"));

            material.updateProgress(newProgress);
            materialRepository.save(material);
            log.info("재료 ID:  {} , 상태 업데이트 완료", materialId);
            return true;
        } catch (Exception e) {
            log.error("재료 상태 업데이트 중 오류 발생. Material ID: {}", materialId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean reduceThickAndWidth(Long materialId) {
        try {
            // 재료 조회
            Materials material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new EntityNotFoundException("Material not found"));

            // 공정과 코일 타입에 맞는 ColdStandardReduction 정보 조회
            ColdStandardReduction reduction = coldStandardReductionRepository.findByCoilTypeCodeAndProcess(
                            material.getCoilTypeCode(), material.getCurrProc())
                    .orElseThrow(() -> new EntityNotFoundException("ColdStandardReduction 정보 없음"));

            // 두께와 폭 감소
            double reduceThickValue = reduction.getThicknessReduction();
            double reduceWidthValue = reduction.getWidthReduction();

            material.updateThickAndWidth(reduceThickValue, reduceWidthValue);

            // 변경된 재료 저장
            materialRepository.save(material);

            log.info("재료 업데이트 성공. Material ID: {}, 두께 감소: {}, 폭 감소: {}", materialId, reduceThickValue, reduceWidthValue);
            return true;
        } catch (Exception e) {
            log.error("재료 두께와 폭 감소 중 오류 발생. Material ID: {}", materialId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateProcess(Long materialId) {
        try {
            Materials material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new EntityNotFoundException("Material not found"));

            material.updateMaterialProgress();
            materialRepository.save(material);
            log.info("재료 ID:  {} , 공정 업데이트 완료", materialId);
            return true;
        } catch (Exception e) {
            log.error("재료 공정 업데이트 중 오류 발생. Material ID: {}", materialId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateYard(Long materialId) {
        try {
            Materials material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new EntityNotFoundException("Material not found"));

            material.updateYardAfterWork("B");
            materialRepository.save(material);
            log.info("재료 ID:  {} , 야드 업데이트 완료", materialId);
            return true;
        } catch (Exception e) {
            log.error("야드 위치 업데이트 중 오류 발생. Material ID: {}", materialId, e);
            return false;
        }
    }


}