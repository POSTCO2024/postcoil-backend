package com.postco.schedule.service.impl;

import com.postco.schedule.domain.*;
import com.postco.schedule.domain.repository.SCHConfirmRepository;
import com.postco.schedule.domain.repository.SCHHistoryRepository;
import com.postco.schedule.domain.repository.SCHMaterialRepository;
import com.postco.schedule.domain.repository.SCHPlanRepository;
import com.postco.schedule.presentation.SCHForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 편의 상, 임시로 서비스를 분리했습니다.
 * 코일 순서 변경 및 스케쥴 확정 작업에 대한 서비스 입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanAfterWorkServiceImpl {
    private final SCHMaterialRepository materialRepository;
    private final SCHHistoryRepository historyRepository;
    private final SCHPlanRepository planRepository;
    private final SCHConfirmRepository confirmRepository;

    // 스케쥴 확정 및 코일 순서 업데이트
    //     << 작업 시기 >>
    //      프론트에서 드래그 앤 드롭 할 때마다 업데이트 하지 않음.
    //      확정을 눌러야만 해당 변경사항으로 업데이트 및 기록 저장됨. (매번 트랜잭션을 날리는 것은 부담 )
    @Transactional
    public boolean confirmScheduleWithNewMaterials(List<SCHForm> schForms) {

        try {
            List<SCHHistory> histories = new ArrayList<>();

            for (SCHForm schForm : schForms) {
                Long planId = schForm.getPlanId();
                String confirmBy = schForm.getConfirmBy();
                List<SCHForm.UpdateMaterialInfo> updateMaterials = schForm.getUpdateMaterials();

                log.info("스케줄 업데이트 및 확정 시작. 스케줄 ID: {}", planId);

                SCHPlan schPlan = planRepository.findById(planId)
                        .orElseThrow(() -> new EntityNotFoundException("스케줄 계획을 찾을 수 없습니다. ID: " + planId));

                // 1. 확정(스케쥴 Plan 테이블의 isConfirmed 업데이트)
                // 확정 시에도 이력 테이블에 넣고 싶으면 주석 풀기 ..
                String oldConfirmStatus = schPlan.getIsConfirmed();
                schPlan.confirmSchedule();
                planRepository.save(schPlan);
                // histories.add(createHistory(planId, null, "SCHPlan", "isConfirmed", oldConfirmStatus, schPlan.getIsConfirmed(), confirmBy));
                log.info("스케줄 확정 완료. 스케줄 ID: {}", planId);

                // 2. 코일 순서 변경 있으면, 순서 업데이트 진행
                Optional.ofNullable(updateMaterials)
                        .filter(materials -> !materials.isEmpty())
                        .ifPresentOrElse(
                                materials -> {
                                    updateMaterialSequences(schPlan, materials, confirmBy, histories);
                                    log.info("재료 순서 업데이트 완료. 스케줄 ID: {}", planId);
                                },
                                () -> log.info("재료 순서 업데이트 없음. 스케줄 ID: {}", planId)
                        );

                // 3. 확정 DB 저장에 저장.
                SCHConfirm schConfirm = saveScheduleConfirm(schPlan, confirmBy);
                log.info("스케줄 확정 정보 저장 완료. 확정 ID: {}", schConfirm.getId());

            }
            // 4. 히스토리 추가
            historyRepository.saveAll(histories);
            log.info("모든 변경 이력 추가 완료.");

            log.info("모든 스케줄 업데이트 및 확정 완료.");
            return true;
        } catch (Exception e) {
            log.error("스케줄 업데이트 및 확정 중 오류 발생.", e);
            throw new RuntimeException("스케줄 업데이트 및 확정 실패", e);
        }
    }


    // step 1. 스케쥴 대상재의 sequence 업데이드
    private void updateMaterialSequences(SCHPlan schPlan, List<SCHForm.UpdateMaterialInfo> updateMaterials, String changedBy, List<SCHHistory> histories) {
        log.info("재료 순서 업데이트 시작. 스케줄 ID: {}", schPlan.getId());
        Map<Long, Integer> materialSequences = updateMaterials.stream()
                .collect(Collectors.toMap(SCHForm.UpdateMaterialInfo::getMaterialId, SCHForm.UpdateMaterialInfo::getSequence));

        List<Long> materialIds = updateMaterials.stream()
                .map(SCHForm.UpdateMaterialInfo::getMaterialId)
                .collect(Collectors.toList());

        List<SCHMaterial> materials = materialRepository.findBySchPlanIdAndMaterialIds(schPlan.getId(), materialIds);
        materials.forEach(material -> {
            Integer newSequence = materialSequences.get(material.getId());
            if (newSequence != null && !newSequence.equals(material.getSequence())) {
                String oldValue = String.valueOf(material.getSequence());
                material.updateSequence(newSequence);

                histories.add(createHistory(schPlan.getId(), material.getId(), "SCHMaterial", "sequence", oldValue, String.valueOf(newSequence), changedBy));
                log.info("재료 순서 변경. 재료 ID: {}, 새로운 순서: {}", material.getId(), newSequence);
            }
        });

        // 각 재료의 업데이트가 끝난 후 한 번에 저장 (saveAll 사용)
        materialRepository.saveAll(materials);
    }

    // step 2. 스케쥴 확정 DB 저장
    private SCHConfirm saveScheduleConfirm(SCHPlan schPlan, String confirmBy) {
        log.info("스케줄 확정 정보 생성 및 저장 시작. 스케줄 ID: {}", schPlan.getId());

        SCHConfirm schConfirm = SCHConfirm.builder()
                .scheduleNo(schPlan.getScheduleNo())
                .process(schPlan.getProcess())
                .rollUnit(schPlan.getRollUnit())
                .scExpectedDuration(schPlan.getScExpectedDuration())
                .quantity(schPlan.getQuantity())
                .confirmedBy(confirmBy)
                .confirmDate(LocalDateTime.now())
                .workStatus(WorkStatus.PENDING)
                .build();

        SCHConfirm savedConfirm = confirmRepository.save(schConfirm);

        // SCHConfirm 연관관계 설정
        List<SCHMaterial> materials = schPlan.getMaterials();
        materials.forEach(material -> {
            material.setSchConfirm(savedConfirm);
        });

        materialRepository.saveAll(materials);

        return savedConfirm;
    }


    // step 3. 스케쥴 이력 DB 에 변경 기록 추가
    private SCHHistory createHistory(Long schPlanId, Long schMaterialId, String tableName, String columnName, String oldValue, String newValue, String changedBy) {
        return SCHHistory.builder()
                .schPlanId(schPlanId)
                .schMaterialId(schMaterialId)
                .tableName(tableName)
                .columnName(columnName)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .build();
    }
}
