//package com.postco.schedule.service.impl;
//
//import com.postco.core.utils.mapper.MapperUtils;
//import com.postco.core.utils.mapper.TargetMaterialMapper;
//import com.postco.schedule.domain.PriorityApplyMethod;
//import com.postco.schedule.domain.SCHMaterial;
//import com.postco.schedule.domain.repository.SCHMaterialRepository;
//import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;
//import com.postco.schedule.presentation.dto.PriorityDTO;
//import com.postco.schedule.presentation.dto.SCHMaterialDTO;
//import com.postco.schedule.service.redis.SCHMaterialRedisQueryService;
//import com.postco.schedule.service.redis.SCHMaterialRedisService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class SchedulingServiceImpl {
//
//    private final PriorityServiceImpl priorityService;
//    private final ConstraintInsertionServiceImpl constraintInsertionService;
//    private final SCHMaterialRedisService schMaterialRedisService;
//    private final SCHMaterialRedisQueryService schMaterialRedisQueryService;
//
//
//    // 스케쥴링
//    public List<SCHMaterial> planSchedule(List<SCHMaterial> materials, String processCode) {
//        String rollUnit = materials.get(0).getRollUnit();
//
//        // Redis에서 미편성된 코일 불러오기 (비동기적으로 받아와서 동기적으로 변환)
//        Optional.ofNullable(schMaterialRedisQueryService.fetchUnassignedCoilsByCurrProcAndRollUnit(processCode, rollUnit).block())
//                .ifPresent(unassignedCoilsFromRedis -> {
//                    List<SCHMaterial> unassignedCoils = MapperUtils.mapList(unassignedCoilsFromRedis, SCHMaterial.class);
//
//                    // 기존 materials 리스트에 Redis에서 불러온 미편성 코일 추가
//                    if (!unassignedCoils.isEmpty()) {
//                        materials.addAll(unassignedCoils);
//
//                        // Redis에 저장된 미편성 코일 삭제
//                        List<String> unassignedCoilIds = unassignedCoils.stream()
//                                .map(SCHMaterial::getId)
//                                .map(String::valueOf)
//                                .collect(Collectors.toList());
//
//                        deleteUnassignedCoilsFromRedis(unassignedCoilIds).subscribe(); // 비동기적으로 삭제
//                    }
//                });
//
//
//        // ProcessCode와 RollUnit에 해당하는 제약조건 및 우선순위 조회
//        List<PriorityDTO> priorities = priorityService.findAllByProcessCodeAndRollUnit(processCode, rollUnit);
//        List<ConstraintInsertionDTO> constraintInsertionList = constraintInsertionService.findAllByProcessCodeAndRollUnit(processCode, rollUnit);
//
//        Double standardWidth = 50.0;  // 기본값 처리
//
//        // 제약 조건에서 폭 기준값 설정
//        for (ConstraintInsertionDTO constraint : constraintInsertionList) {
//            if ("CONSTRAINT".equals(constraint.getType()) && "width".equals(constraint.getTargetColumn())) {
//                standardWidth = constraint.getTargetValue();  // 값이 일치하면 targetValue를 저장
//                break;
//            }
//        }
//
//        // 우선순위 적용
//        List<SCHMaterial> sortedMaterials = applyPriorities(materials, priorities, standardWidth);
//
//        // 편성된 코일들에 순서를 할당
//        for (int i = 0; i < sortedMaterials.size(); i++) {
//            sortedMaterials.get(i).setSequence(i + 1);
//        }
//
//        printCurrentState(sortedMaterials, "미편성 처리 전");
//
//        // 제약 조건을 적용하여 편성된 코일 필터링
//        List<SCHMaterial> filteredCoils = applyConstraintToCoils(sortedMaterials, constraintInsertionList);
//
//        printCurrentState(filteredCoils, "미편성 처리 후");
//
//        // 미편성된 코일들(편성에서 제외된 코일들)
//        List<SCHMaterial> unassignedCoilsAfterProcessing = sortedMaterials.stream()
//                .filter(coil -> !filteredCoils.contains(coil))
//                .collect(Collectors.toList());
//
//        // Redis에 새로 미편성된 코일 저장 (비동기적으로 저장)
//        saveUnassignedCoilsToRedis(unassignedCoilsAfterProcessing).subscribe();
//
//        // 미편성 삽입
//       // Redis에서 미편성된 코일 불러오기 (비동기적으로 받아와서 동기적으로 변환)
//        Optional.ofNullable(schMaterialRedisQueryService.fetchUnassignedCoilsByCurrProcAndRollUnit(processCode, rollUnit).block())
//                .ifPresent(unassignedCoilsFromRedis -> {
//                    List<SCHMaterial> unassignedCoils = MapperUtils.mapList(unassignedCoilsFromRedis, SCHMaterial.class);
//                    if (!unassignedCoils.isEmpty()) {
//                        // Redis에 저장된 미편성 코일 삭제
//                        List<String> unassignedCoilIds = unassignedCoils.stream()
//                                .map(SCHMaterial::getId)
//                                .map(String::valueOf)
//                                .collect(Collectors.toList());
//                        deleteUnassignedCoilsFromRedis(unassignedCoilIds).subscribe(); // 비동기적으로 삭제
//                    }
//                });
//
//        List<SCHMaterial> finalCoilList = insertUnassignedCoilsBackToSchedule(filteredCoils, unassignedCoilsAfterProcessing, constraintInsertionList);
//        printCurrentState(finalCoilList, "미편성 삽입 후");
//
//        return finalCoilList;  // 최종 편성된 코일을 반환
//    }
//
//
//    // 우선 순위에 따라 순서대로 스케쥴링 진행하는 함수
//    private List<SCHMaterial> applyPriorities(List<SCHMaterial> materials,
//                                              List<PriorityDTO> priorities, Double standardWidth) {
//
//        List<SCHMaterial> prioritizedMaterials = new ArrayList<>();
//        List<SCHMaterial> sortedMaterials = new ArrayList<>();
//        List<List<SCHMaterial>> groupedMaterials = new ArrayList<>();
//
//
//        for (PriorityDTO priority : priorities) {
//            PriorityApplyMethod method = PriorityApplyMethod.valueOf(priority.getApplyMethod());
//
//            switch (method) {
//                case DESC_GOALWIDTH:
//                    sortedMaterials = sortedWidthDesc(materials);
//                    break;
//
//                case GROUPING_BY_GOALWIDTH:
//                    groupedMaterials = groupByWidth(sortedMaterials,standardWidth);
//                    break;
//
//                case ASC_THICKNESS:
//                    groupedMaterials = sortEachGroupByThicknessAsc(groupedMaterials);
//                    break;
//
//                case APPLY_SIN:
//                    groupedMaterials = applySineCurveToGroups(groupedMaterials);
//                    break;
//
//                default:
//                    prioritizedMaterials = groupedMaterials.stream()
//                            .flatMap(List::stream)
//                            .collect(Collectors.toList());
//                    return prioritizedMaterials;
//            }
//        }
//
//        return prioritizedMaterials;
//    }
//
//    // 폭 기준 내림차순 함수
//    private List<SCHMaterial> sortedWidthDesc(List<SCHMaterial> coils){
//        List<SCHMaterial> result = new ArrayList<>();
//        result= coils.stream()
//                .sorted(Comparator.comparingDouble(SCHMaterial::getGoalWidth).reversed())
//                .collect(Collectors.toList());
//
//
//        return result;
//    }
//
//    // 폭 기준 동일폭 기준에 맞춰 그룹핑
//    private List<List<SCHMaterial>> groupByWidth(List<SCHMaterial> sortedCoils, Double standardWidth){
//        List<List<SCHMaterial>> coilGroups = new ArrayList<>();
//        List<SCHMaterial> currentCoilGroup = new ArrayList<>();
//
//        // 첫번째 코일 기준 start
//        SCHMaterial firstCoil = sortedCoils.get(0);
//        currentCoilGroup.add(firstCoil);
//        double currentBaseWidth = firstCoil.getGoalWidth();
//
//        // 그룹핑 로직
//        for(int i = 1; i < sortedCoils.size(); i++){
//            SCHMaterial currentCoil = sortedCoils.get(i);
//            if (currentBaseWidth - currentCoil.getGoalWidth() <= standardWidth) {
//                currentCoilGroup.add(currentCoil);
//            } else {
//                coilGroups.add(new ArrayList<>(currentCoilGroup));
//                currentCoilGroup.clear();
//                currentCoilGroup.add(currentCoil);
//                currentBaseWidth = currentCoil.getGoalWidth();
//            }
//        }
//        coilGroups.add(currentCoilGroup);
//
//        return coilGroups;
//    }
//
//    // 폭 기준 동일폭 그룹들을 각각 두께 오름차순
//    private List<List<SCHMaterial>> sortEachGroupByThicknessAsc(List<List<SCHMaterial>> groupCoils) {
//        return groupCoils.stream()
//                .map(group -> group.stream()
//                        .sorted(Comparator.comparingDouble(SCHMaterial::getThickness))
//                        .collect(Collectors.toList())
//                ).collect(Collectors.toList());
//    }
//
//    // 두께 배치에 sin그래프 적용
//    private List<List<SCHMaterial>> applySineCurveToGroups(List<List<SCHMaterial>> groupCoils) {
//
//    List<List<SCHMaterial>> optimizedGroups = new ArrayList<>();
//    double previousEndThickness = 0.0;  // 초기값
//
//    for (List<SCHMaterial> group : groupCoils) {
//        final double currentPreviousEndThickness = previousEndThickness;
//
//        // 1) 두께가 증가하는 코일과 감소하는 코일을 구분
//        List<SCHMaterial> increasingCoils = group.stream()
//                .filter(coil -> coil.getThickness() >= currentPreviousEndThickness)
//                .sorted(Comparator.comparingDouble(coil -> Math.abs(coil.getThickness() - currentPreviousEndThickness)))
//                .collect(Collectors.toList());
//
//        List<SCHMaterial> decreasingCoils = group.stream()
//                .filter(coil -> coil.getThickness() < currentPreviousEndThickness)
//                .sorted(Comparator.comparingDouble(coil -> Math.abs(coil.getThickness() - currentPreviousEndThickness)))
//                .collect(Collectors.toList());
//
//        // 2) 감소 방향 먼저, 증가 방향 나중에 배치
//        List<SCHMaterial> sortedGroup = new ArrayList<>(increasingCoils);
//        sortedGroup.addAll(decreasingCoils);
//
//        // 3) 최적화된 그룹 선택
//        List<SCHMaterial> bestOptimizedGroup = findBestOptimizedGroup(sortedGroup);
//
//        // 마지막 코일 두께 업데이트
//        previousEndThickness = bestOptimizedGroup.get(bestOptimizedGroup.size() - 1).getThickness();
//
//        // 최적화된 그룹 추가
//        optimizedGroups.add(bestOptimizedGroup);
//    }
//
//    return optimizedGroups;
//}
//
//    // 최적 그룹을 찾는 메서드
//    private List<SCHMaterial> findBestOptimizedGroup(List<SCHMaterial> sortedGroup) {
//        List<SCHMaterial> bestOptimizedGroup = null;
//        double minThicknessDifferenceSum = Double.MAX_VALUE;
//
//        for (int waveType = 1; waveType <= 5; waveType++) {
//            // 여러 사인 곡선
//            double[] sineWave = generatedSineWave(sortedGroup.size(), waveType);
//
//            // 사인 곡선에 맞게 재배치
//            List<SCHMaterial> optimizedGroup = rearrangeBySineWave(sortedGroup, sineWave);
//
//            // 두께 차이의 총합 계산
//            double thicknessDifferenceSum = calculateThicknessDifferenceSum(optimizedGroup);
//
//            // 최소 두께 차이 그룹 선택
//            if (thicknessDifferenceSum < minThicknessDifferenceSum) {
//                minThicknessDifferenceSum = thicknessDifferenceSum;
//                bestOptimizedGroup = optimizedGroup;
//            }
//        }
//
//        return bestOptimizedGroup;
//    }
//
//    // 사인 곡선에 맞게 그룹을 재배치하는 메서드
//    private List<SCHMaterial> rearrangeBySineWave(List<SCHMaterial> sortedGroup, double[] sineWave) {
//        List<Integer> sineIndices = new ArrayList<>();
//        for (int i = 0; i < sineWave.length; i++) {
//            sineIndices.add(i);
//        }
//
//        // 사인 곡선에 따라 인덱스 정렬
//        sineIndices.sort(Comparator.comparing(i -> sineWave[i]));
//
//        // 재배치된 그룹 생성
//        List<SCHMaterial> optimizedGroup = new ArrayList<>();
//        for (int i = 0; i < sortedGroup.size(); i++) {
//            optimizedGroup.add(sortedGroup.get(sineIndices.get(i)));
//        }
//
//        return optimizedGroup;
//    }
//
//    // 두께 차이의 총합을 계산하는 메서드 (최적 sin 찾을 때 사용)
//    private double calculateThicknessDifferenceSum(List<SCHMaterial> group) {
//        double thicknessDifferenceSum = 0;
//        for (int i = 0; i < group.size() - 1; i++) {
//            thicknessDifferenceSum += Math.abs(group.get(i).getThickness() - group.get(i + 1).getThickness());
//        }
//        return thicknessDifferenceSum;
//    }
//
//    // sin 곡선 그리는 함수
//    public double[] generatedSineWave(int size, int waveType) {
//        double[] sineWave = new double[size];
//
//        switch (waveType) {
//            case 1:
//                // 기본적인 sin 곡선 (0에서 π까지의 값을 사용)
//                for (int i = 0; i < size; i++) {
//                    sineWave[i] = Math.sin(Math.PI * i / (size - 1));
//                }
//                break;
//
//            case 2:
//                // 2π 범위의 sin 곡선 (0에서 2π까지의 값을 사용)
//                for (int i = 0; i < size; i++) {
//                    sineWave[i] = Math.sin(2 * Math.PI * i / (size - 1));
//                }
//                break;
//
//            case 3:
//                // 좀 더 급격하게 변화하는 sin 곡선 (0에서 π/2까지의 값을 사용)
//                for (int i = 0; i < size; i++) {
//                    sineWave[i] = Math.sin(Math.PI / 2 * i / (size - 1));
//                }
//                break;
//
//            case 4:
//                // sin 곡선의 음수 (내려갔다가 올라가는 형태, 0에서 π까지)
//                for (int i = 0; i < size; i++) {
//                    sineWave[i] = -Math.sin(Math.PI * i / (size - 1));
//                }
//                break;
//
//            case 5:
//                // cos 곡선 사용 (내려갔다가 올라가는 형태)
//                for (int i = 0; i < size; i++) {
//                    sineWave[i] = Math.cos(Math.PI * i / (size - 1));
//                }
//                break;
//
//            default:
//                // 기본적으로 sin(0에서 π까지) 사용
//                for (int i = 0; i < size; i++) {
//                    sineWave[i] = Math.sin(Math.PI * i / (size - 1));
//                }
//                break;
//        }
//
//        return sineWave;
//    }
//
//
//    // 제약 조건 적용 함수
//    private List<SCHMaterial> applyConstraintToCoils(List<SCHMaterial> coils, List<ConstraintInsertionDTO> constraintInsertionList) {
//        // thickness 제약조건 추출
//        Double thicknessConstraintValue = constraintInsertionList.stream()
//                .filter(constraint -> "thickness".equals(constraint.getTargetColumn()) && "CONSTRAINT".equals(constraint.getType()))
//                .map(ConstraintInsertionDTO::getTargetValue)
//                .findFirst()
//                .orElse(null);  // 없으면 null 반환
//
//        // 제약조건이 없으면 원래 리스트 반환
//        if (thicknessConstraintValue == null) {
//            return coils;
//        }
//
//        boolean hasConstraintViolation;
//        List<SCHMaterial> filteredCoils = new ArrayList<>(coils);
//        List<SCHMaterial> unAssignedCoilsForRedis = new ArrayList<>();
//        // 제약조건 위반이 없을 때까지 반복
//        do {
//            hasConstraintViolation = false;  // 매 반복 시 초기화
//
//            // 미편성된 코일 리스트를 필터링
//            List<SCHMaterial> unassignedCoils = new ArrayList<>();
//            for (int i = 1; i < filteredCoils.size(); i++) {  // 첫 번째 코일은 건너뜀
//                SCHMaterial currentCoil = filteredCoils.get(i);
//                SCHMaterial previousCoil = filteredCoils.get(i - 1);
//                double thicknessDifference = Math.abs(previousCoil.getThickness() - currentCoil.getThickness());
//
//                if (thicknessDifference >= thicknessConstraintValue) {
//                    unassignedCoils.add(currentCoil);  // 제약 조건을 위반한 코일 추가
//                    hasConstraintViolation = true;  // 제약조건 위반 발생
//                }
//            }
//
//            // 미편성된 코일을 제외한 리스트 생성
//            filteredCoils = filteredCoils.stream()
//                    .filter(coil -> !unassignedCoils.contains(coil))  // 미편성된 코일을 제거
//                    .collect(Collectors.toList());
//
//            // 로그 출력
//            if (!unassignedCoils.isEmpty()) {
//                System.out.println("미편성된 코일:");
//                unassignedCoils.forEach(coil -> System.out.println("ID: " + coil.getId() + ", Thickness: " + coil.getThickness() + ", Width: " + coil.getGoalWidth()));
//                unAssignedCoilsForRedis.addAll(unassignedCoils);
//
//            }
//
//        } while (hasConstraintViolation);  // 제약 조건을 위반하는 코일이 없을 때까지 반복
//
//        if(!unAssignedCoilsForRedis.isEmpty()){
//            // 미편성된 코일을 Redis에 저장
//            saveUnassignedCoilsToRedis(unAssignedCoilsForRedis);
//        }
//        return filteredCoils;
//    }
//
//
//    // 미편성 삽입 함수
//    private List<SCHMaterial> insertUnassignedCoilsBackToSchedule(List<SCHMaterial> scheduledCoils, List<SCHMaterial> unassignedCoils,
//                                                                  List<ConstraintInsertionDTO> constraintInsertionList) {
//        List<SCHMaterial> finalCoilList = new ArrayList<>(scheduledCoils); // 기존 스케줄링된 코일 리스트
//        List<SCHMaterial> remainingUnassignedCoils = new ArrayList<>(); // 삽입되지 않은 미편성 코일 리스트
//        Double flagWidth = 50.0; // 기본 값
//        Double flagThickness = 1.0; // 기본 값
//
//        for (ConstraintInsertionDTO constraint : constraintInsertionList) {
//            if ("INSERTION".equals(constraint.getType()) && "width".equals(constraint.getTargetColumn())) {
//                flagWidth = constraint.getTargetValue();
//            }
//            if ("CONSTRAINT".equals(constraint.getType()) && "thickness".equals(constraint.getTargetColumn())) {
//                flagThickness = constraint.getTargetValue();
//            }
//        }
//
//        for (SCHMaterial unassignedCoil : unassignedCoils) {
//            boolean inserted = false; // 삽입 여부 추적
//
//            // 스케줄링된 코일 리스트를 순회하면서 적절한 위치를 찾아서 삽입
//            for (int i = 0; i < finalCoilList.size(); i++) {
//                if (i == 0) {
//                    SCHMaterial nextCoil = finalCoilList.get(i);
//                    if (Math.abs(nextCoil.getGoalWidth() - unassignedCoil.getGoalWidth()) <= flagWidth
//                            && Math.abs(nextCoil.getThickness() - unassignedCoil.getThickness()) <= flagThickness) {
//
//                        // 적절한 위치에 미편성 코일 삽입
//                        finalCoilList.add(i, unassignedCoil);
//                        inserted = true; // 삽입되었음을 기록
//                        log.info("Unassigned coil (ID: {}) inserted between coils (ID: {}) at position {}",
//                                unassignedCoil.getId(), nextCoil.getId(), i);
//                        break;
//                    }
//                } else {
//                    SCHMaterial previousCoil = finalCoilList.get(i - 1);
//                    SCHMaterial nextCoil = finalCoilList.get(i);
//
//                    if ((previousCoil.getGoalWidth() + flagWidth) <= unassignedCoil.getGoalWidth()
//                            && unassignedCoil.getGoalWidth() <= (nextCoil.getGoalWidth() + flagWidth)
//                            && Math.abs(previousCoil.getThickness() - unassignedCoil.getThickness()) <= flagThickness
//                            && Math.abs(nextCoil.getThickness() - unassignedCoil.getThickness()) <= flagThickness) {
//
//                        // 적절한 위치에 미편성 코일 삽입
//                        finalCoilList.add(i, unassignedCoil);
//                        inserted = true; // 삽입되었음을 기록
//                        log.info("미편성코일 (ID: {})이 (ID: {})와 (ID: {}) 사이에 삽입되었습니다. {}",
//                                unassignedCoil.getId(), previousCoil.getId(), nextCoil.getId(), i);
//                        break;
//                    }
//                }
//            }
//
//            // 삽입이 되지 않았을 경우 남은 코일로 저장
//            if (!inserted) {
//                log.warn("미편성코일 (ID: {}) 삽입불가", unassignedCoil.getId());
//                remainingUnassignedCoils.add(unassignedCoil); // 삽입되지 않은 코일 저장
//            }
//        }
//
//        // 남은 미편성 코일을 Redis에 다시 저장
//        if (!remainingUnassignedCoils.isEmpty()) {
//            saveUnassignedCoilsToRedis(remainingUnassignedCoils).subscribe(); // 비동기적으로 Redis에 저장
//            log.info("남은 미편성 코일들 저장됨. Count: {}", remainingUnassignedCoils.size());
//        }
//
//        return finalCoilList;
//    }
//
//
//    // Helper method to capitalize the first letter of the field name
//    public String convertSnakeToPascal(String snakeCaseString) {
//        // Check for null or empty input
//        if (snakeCaseString == null || snakeCaseString.isEmpty()) {
//            return snakeCaseString;
//        }
//
//        // Split the string by underscores
//        String[] parts = snakeCaseString.split("_");
//
//        // Convert each part to Title Case
//        StringBuilder pascalCaseString = new StringBuilder();
//        for (String part : parts) {
//            if (!part.isEmpty()) {
//                pascalCaseString.append(Character.toUpperCase(part.charAt(0)))
//                        .append(part.substring(1).toLowerCase());
//            }
//        }
//        return pascalCaseString.toString();
//    }
//
//
//    public List<SCHMaterial> insertMaterialsWithWorkTime(List<SCHMaterial> materials) {
//        for (SCHMaterial material : materials) {
//            // 작업 시간 계산
//            Long workTime = calculateWorkTime(material.getGoalLength(), material.getGoalThickness(),
//                    material.getGoalWidth(), material.getTotalWeight());
//            //material.setWorkTime(workTime);
//        }
//
//        return materials;
//    }
//
//    // 작업 시간 계산 메서드
//    private Long calculateWorkTime(double goalLength, double goalThickness, double goalWidth, double totalWeight) {
//        return  (long) ((goalLength * goalThickness * goalWidth) / totalWeight);
//    }
//
//    // 결과값 확인하기 위한 함수
//    private void printCurrentState(List<SCHMaterial> materials, String message) {
//        log.info(message);
//        for (SCHMaterial material : materials) {
//            log.info("ID: {}, Goal Width: {}, Thickness: {}, Temperature: {}, RollUnit: {}",
//                    material.getId(), material.getGoalWidth(), material.getThickness(), material.getTemperature(), material.getRollUnit());
//
//        }
//    }
//
//
//    // Redis에 미편성된 코일 저장 로직
//    private Mono<Void> saveUnassignedCoilsToRedis(List<SCHMaterial> unassignedCoils) {
//        return Flux.fromIterable(unassignedCoils)
//                .flatMap(coil -> schMaterialRedisService.saveData(coil)
//                        .doOnSuccess(result -> {
//                            if (result) {
//                                log.info("미편성된 코일 저장 완료 - ID: {}", coil.getId());
//                            } else {
//                                log.warn("미편성된 코일 저장 실패 - ID: {}", coil.getId());
//                            }
//                        })
//                        .doOnError(error -> log.error("Redis 저장 중 오류 발생 - ID: {}: {}", coil.getId(), error.getMessage())))
//                .then()
//                .doOnSuccess(unused -> log.info("모든 미편성 코일이 Redis에 저장되었습니다."))
//                .doOnError(error -> log.error("미편성 코일 저장 중 전체 오류 발생: {}", error.getMessage()));
//    }
//
//    private Mono<Void> deleteUnassignedCoilsFromRedis(List<String> unassignedCoilIds) {
//        return Flux.fromIterable(unassignedCoilIds)
//                .flatMap(id -> schMaterialRedisService.deleteData(id)
//                        .doOnSuccess(result -> {
//                            if (result) {
//                                log.info("미편성된 코일 삭제 완료 - ID: {}", id);
//                            } else {
//                                log.warn("미편성된 코일 삭제 실패 - ID: {}", id);
//                            }
//                        })
//                        .doOnError(error -> log.error("Redis 삭제 중 오류 발생 - ID: {}: {}", id, error.getMessage())))
//                .then();
//    }
//
//}