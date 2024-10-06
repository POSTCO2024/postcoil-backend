package com.postco.schedule.service.impl;


import com.postco.schedule.domain.PriorityApplyMethod;
import com.postco.schedule.domain.SCHMaterial;
import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;
import com.postco.schedule.presentation.dto.PriorityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingServiceImplRefac {

    private final PriorityServiceImpl priorityService;
    private final ConstraintInsertionServiceImpl constraintInsertionService;
    private final RedisServiceImpl redisService;  // Redis 관련 로직 분리

    // 스케쥴링 로직
    public List<SCHMaterial> planSchedule(List<SCHMaterial> materials, String processCode) {
        String rollUnit = materials.get(0).getRollUnit();

//        // Redis에서 미편성된 코일 불러오기
//        List<SCHMaterial> unassignedCoils = redisService.fetchUnassignedCoils(processCode, rollUnit);
//        materials.addAll(unassignedCoils);
//
//        // 불러온 미편성 코일 삭제
//        redisService.deleteUnassignedCoils(unassignedCoils).subscribe();

        // ProcessCode와 RollUnit에 해당하는 제약조건 및 우선순위 조회
        List<PriorityDTO> priorities = priorityService.findAllByProcessCodeAndRollUnit(processCode, rollUnit);
        List<ConstraintInsertionDTO> constraintInsertionList = constraintInsertionService.findAllByProcessCodeAndRollUnit(processCode, rollUnit);

        // 우선순위 및 제약 조건 적용
        List<SCHMaterial> sortedMaterials = applyPriorities(materials, priorities, 50.0);
        printCurrentState(sortedMaterials, "미편성 처리 전");

        // 제약 조건을 적용하여 편성된 코일 필터링
        List<SCHMaterial> filteredCoils = applyConstraintToCoils(sortedMaterials, constraintInsertionList);
        printCurrentState(filteredCoils, "미편성 처리 후");

        // 미편성된 코일을 다시 Redis에 저장
        List<SCHMaterial> unassignedCoilsAfterProcessing = sortedMaterials.stream()
                .filter(coil -> !filteredCoils.contains(coil))
                .collect(Collectors.toList());
        redisService.saveUnassignedCoils(unassignedCoilsAfterProcessing).subscribe();

        // 미편성된 코일 다시 스케줄에 삽입
        List<SCHMaterial> finalCoilList = insertUnassignedCoilsBackToSchedule(filteredCoils, unassignedCoilsAfterProcessing, constraintInsertionList);
        printCurrentState(finalCoilList, "미편성 삽입 후");

        AtomicInteger sequence = new AtomicInteger(1); // 시퀀스 시작 값을 1로 설정
        finalCoilList.forEach(coil -> coil.setSequence(sequence.getAndIncrement()));

        return finalCoilList;  // 최종 편성된 코일 반환
    }

    // 우선순위 적용 로직
    private List<SCHMaterial> applyPriorities(List<SCHMaterial> materials, List<PriorityDTO> priorities, Double standardWidth) {
        List<SCHMaterial> prioritizedMaterials = new ArrayList<>();
        List<SCHMaterial> sortedMaterials = new ArrayList<>();
        List<List<SCHMaterial>> groupedMaterials = new ArrayList<>();

        for (PriorityDTO priority : priorities) {
            PriorityApplyMethod method = PriorityApplyMethod.valueOf(priority.getApplyMethod());

            switch (method) {
                case DESC_GOALWIDTH:
                    sortedMaterials = SchedulingUtils.sortedWidthDesc(materials);
                    break;

                case GROUPING_BY_GOALWIDTH:
                    groupedMaterials = SchedulingUtils.groupByWidth(sortedMaterials, standardWidth);
                    break;

                case ASC_THICKNESS:
                    groupedMaterials = SchedulingUtils.sortEachGroupByThicknessAsc(groupedMaterials);
                    break;

                case APPLY_SIN:
                    groupedMaterials = SchedulingUtils.applySineCurveToGroups(groupedMaterials);
                    break;

                default:
                    prioritizedMaterials = groupedMaterials.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    return prioritizedMaterials;
            }
        }

        return prioritizedMaterials;
    }

    // 제약 조건을 적용하여 편성된 코일 필터링
    private List<SCHMaterial> applyConstraintToCoils(List<SCHMaterial> coils, List<ConstraintInsertionDTO> constraintInsertionList) {
        Double thicknessConstraintValue = constraintInsertionList.stream()
                .filter(constraint -> "thickness".equals(constraint.getTargetColumn()) && "CONSTRAINT".equals(constraint.getType()))
                .map(ConstraintInsertionDTO::getTargetValue)
                .findFirst()
                .orElse(null);

        if (thicknessConstraintValue == null) {
            return coils;
        }

        boolean hasConstraintViolation;
        List<SCHMaterial> filteredCoils = new ArrayList<>(coils);
        List<SCHMaterial> unAssignedCoilsForRedis = new ArrayList<>();

        do {
            hasConstraintViolation = false;
            List<SCHMaterial> unassignedCoils = new ArrayList<>();

            for (int i = 1; i < filteredCoils.size(); i++) {
                SCHMaterial currentCoil = filteredCoils.get(i);
                SCHMaterial previousCoil = filteredCoils.get(i - 1);
                double thicknessDifference = Math.abs(previousCoil.getThickness() - currentCoil.getThickness());

                if (thicknessDifference >= thicknessConstraintValue) {
                    unassignedCoils.add(currentCoil);
                    hasConstraintViolation = true;
                }
            }

            filteredCoils = filteredCoils.stream()
                    .filter(coil -> !unassignedCoils.contains(coil))
                    .collect(Collectors.toList());

            if (!unassignedCoils.isEmpty()) {
                unAssignedCoilsForRedis.addAll(unassignedCoils);
            }

        } while (hasConstraintViolation);

        if (!unAssignedCoilsForRedis.isEmpty()) {
            redisService.saveUnassignedCoils(unAssignedCoilsForRedis).subscribe();
        }

        return filteredCoils;
    }

    // 미편성 코일을 다시 스케줄에 삽입
    private List<SCHMaterial> insertUnassignedCoilsBackToSchedule(List<SCHMaterial> scheduledCoils, List<SCHMaterial> unassignedCoils,
                                                                  List<ConstraintInsertionDTO> constraintInsertionList) {
        // 불러온 미편성 코일 삭제
        redisService.deleteUnassignedCoils(unassignedCoils).subscribe();

        List<SCHMaterial> finalCoilList = new ArrayList<>(scheduledCoils);
        List<SCHMaterial> remainingUnassignedCoils = new ArrayList<>();
        Double flagWidth = 50.0;
        Double flagThickness = 1.0;

        for (ConstraintInsertionDTO constraint : constraintInsertionList) {
            if ("INSERTION".equals(constraint.getType()) && "goal_width".equals(constraint.getTargetColumn())) {
                flagWidth = constraint.getTargetValue();
            }
            if ("CONSTRAINT".equals(constraint.getType()) && "thickness".equals(constraint.getTargetColumn())) {
                flagThickness = constraint.getTargetValue();
            }
        }

        for (SCHMaterial unassignedCoil : unassignedCoils) {
            boolean inserted = false;

            for (int i = 0; i < finalCoilList.size(); i++) {
                if (SchedulingUtils.canInsertCoil(finalCoilList, i, unassignedCoil, flagWidth, flagThickness)) {
                    finalCoilList.add(i, unassignedCoil);
                    inserted = true;
                    break;
                }
            }

            if (!inserted) {
                remainingUnassignedCoils.add(unassignedCoil);
            }
        }

        if (!remainingUnassignedCoils.isEmpty()) {
            redisService.saveUnassignedCoils(remainingUnassignedCoils).subscribe();
        }

        return finalCoilList;
    }

    // 스케줄 상태 출력 메서드
    private void printCurrentState(List<SCHMaterial> materials, String message) {
        log.info(message);
        for (SCHMaterial material : materials) {
            log.info("ID: {}, Goal Width: {}, Thickness: {}, Temperature: {}, RollUnit: {}, sequence: {}",
                    material.getId(), material.getGoalWidth(), material.getThickness(), material.getTemperature(), material.getRollUnit(), material.getSequence());
        }
    }
}
