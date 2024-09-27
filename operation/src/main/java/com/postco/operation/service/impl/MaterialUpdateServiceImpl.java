package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.MaterialProgress;
import com.postco.operation.domain.entity.Materials;
import com.postco.operation.domain.entity.coil.ColdStandardReduction;
import com.postco.operation.domain.repository.ColdStandardReductionRepository;
import com.postco.operation.domain.repository.MaterialRepository;
import com.postco.operation.service.MaterialUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

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

            material.updateEntireProgress();
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
    public boolean updateYard(Long materialId, String workValue) {
        try {
            Materials material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new EntityNotFoundException("Material not found"));

            material.updateYardAfterWork(workValue);
            materialRepository.save(material);
            log.info("재료 ID:  {} , 야드 업데이트 완료", materialId);
            return true;
        } catch (Exception e) {
            log.error("야드 위치 업데이트 중 오류 발생. Material ID: {}", materialId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateAfterDelivery(Long materialId) {
        try {
            Materials material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new EntityNotFoundException("Material not found"));

            material.finishDelivery();
            materialRepository.save(material);
            log.info("재료 ID:  {} , 배송 완료 후 코일 업데이트 완료", materialId);
            return true;
        } catch (Exception e) {
            log.error("배송 후 재료 업데이트 중 오류 발생. Material ID: {}", materialId, e);
            return false;
        }
    }


}