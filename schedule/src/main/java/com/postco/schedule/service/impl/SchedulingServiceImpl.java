package com.postco.schedule.service.impl;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.PriorityApplyMethod;
import com.postco.schedule.domain.SCHMaterial;
import com.postco.schedule.domain.repository.SCHMaterialRepository;
import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;
import com.postco.schedule.presentation.dto.PriorityDTO;
import com.postco.schedule.presentation.dto.SCHMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingServiceImpl {

    private final SCHMaterialRepository scheduleMaterialsRepository;

    private final PriorityServiceImpl priorityService;
    private final ConstraintInsertionServiceImpl constraintInsertionService;

    private final double STANDARD_WIDTH = 50;

    //* 스케줄링로직~! */
    public List<SCHMaterial> planSchedule(List<SCHMaterial> materials, String processCode) {
        // TODO: Redis Cache-server에서 List<ScheduleMaterialsDTO.Target>로 materialIds에 해당하는 재료들 불러오기
        //  scheduleMaterialsRepository.findAllById(materialIds) -> (cache에서 가져 온) materials로 변경


       // List<SCHMaterial> materials = MapperUtils.mapList(scheduleMaterialsRepository.findAllById(materialIds), SCHMaterial.class); // 나중에 삭제하기
        List<PriorityDTO> priorities = priorityService.findAllByProcessCode(processCode);
        // TODO: Constraints 추가하기
        List<ConstraintInsertionDTO> constraintInsertionList = constraintInsertionService.findByProcessCode("1CAL");

        log.info("Here is constraintList=================================");
        log.info(constraintInsertionList.toString());


        // 우선순위 적용
        List<SCHMaterial> sortedMaterials = applyPriorities(materials, priorities);

//        // priorityOrder 설정
//        for (int i = 0; i < sortedMaterials.size(); i++) {
//            sortedMaterials.get(i).setSequence(Collections.singletonList(i + 1));
//        }

        // 제약 조건 적용 후 미편성 처리
        List<SCHMaterial> filteredCoils = applyConstraintToCoils(sortedMaterials, constraintInsertionList);


        return filteredCoils;
    }

    // 폭 기준 내림차순 함수
    private List<SCHMaterial> sortedWidthDesc(List<SCHMaterial> coils){
        List<SCHMaterial> result = new ArrayList<>();
        result= coils.stream()
                .sorted(Comparator.comparingDouble(SCHMaterial::getGoalWidth).reversed())
                .collect(Collectors.toList());


        return result;
    }

    // 폭 기준 동일폭 기준에 맞춰 그룹핑
    private List<List<SCHMaterial>> groupByWidth(List<SCHMaterial> sortedCoils){
        List<List<SCHMaterial>> coilGroups = new ArrayList<>();
        List<SCHMaterial> currentCoilGroup = new ArrayList<>();

        // 첫번째 코일 기준 start
        SCHMaterial firstCoil = sortedCoils.get(0);
        currentCoilGroup.add(firstCoil);
        double currentBaseWidth = firstCoil.getGoalWidth();

        // 그룹핑 로직
        for(int i = 1; i < sortedCoils.size(); i++){
            SCHMaterial currentCoil = sortedCoils.get(i);
            if (currentBaseWidth - currentCoil.getGoalWidth() <= STANDARD_WIDTH) {
                currentCoilGroup.add(currentCoil);
            } else {
                coilGroups.add(new ArrayList<>(currentCoilGroup));
                currentCoilGroup.clear();
                currentCoilGroup.add(currentCoil);
                currentBaseWidth = currentCoil.getGoalWidth();
            }
        }
        coilGroups.add(currentCoilGroup);
//
//        for(List<SCHMaterial> value : coilGroups) {
//            // 각 그룹의 goalWidth 값을 추출하여 출력
//            String goalWidths = value.stream()
//                    .map(coil -> String.valueOf(coil.getGoalWidth()))  // 각 View 객체에서 goalWidth 추출
//                    .collect(Collectors.joining(", "));  // 콤마로 구분된 문자열로 변환
//
//
//        }

        return coilGroups;
    }

    // 폭 기준 동일폭 그룹들을 각각 두께 오름차순
    private List<List<SCHMaterial>> sortEachGroupByThicknessAsc
    (List<List<SCHMaterial>> groupCoils) {

        List<List<SCHMaterial>> result = new ArrayList<>();

        result = groupCoils.stream()
                .map(group -> group.stream()
                        .sorted(Comparator.comparingDouble(SCHMaterial::getThickness))
                        .collect(Collectors.toList())
                ).collect(Collectors.toList());
//
//        for(List<SCHMaterial> value : result) {
//            // 각 그룹의 goalWidth 값을 추출하여 출력
//            String goalWidths = value.stream()
//                    .map(coil -> String.valueOf(coil.getThickness()))  // 각 View 객체에서 goalWidth 추출
//                    .collect(Collectors.joining(", "));  // 콤마로 구분된 문자열로 변환
//
//
//        }


        return result;
    }

    // 각 그룹들 sin 그래프로 배치
    private List<List<SCHMaterial>> applySineCurveToGroups
    (List<List<SCHMaterial>> groupCoils){

        List<List<SCHMaterial>> optimizedGroups = new ArrayList<>();
        List<Double> prevGroupLastThickness = new ArrayList<>();
        prevGroupLastThickness.add(0.0); // 초기값 설정

        for(List<SCHMaterial> group : groupCoils){
            if (prevGroupLastThickness.get(0) != 0.0) {
                group = group.stream()
                        .sorted(Comparator.comparingDouble(coil -> Math.abs(coil.getThickness() - prevGroupLastThickness.get(0))))
                        .collect(Collectors.toList());
            }

            // sin 곡선 값 생성
            double[] sineWave = generatedSineWave(group.size());
            List<Integer> sineIndices = new ArrayList<>();
            for (int i = 0; i < sineWave.length; i++) {
                sineIndices.add(i);
            }

            // sin 곡선에 따라 배치
            sineIndices.sort(Comparator.comparing(i -> sineWave[i]));
            List<SCHMaterial> optimizedGroup = new ArrayList<>();
            for (int i = 0; i < group.size(); i++) {
                optimizedGroup.add(group.get(sineIndices.get(i)));
            }

            // 마지막 코일 두께를 리스트로 업데이트
            prevGroupLastThickness.set(0, optimizedGroup.get(optimizedGroup.size() - 1).getThickness());

            optimizedGroups.add(optimizedGroup);
        }
//
//        for(List<SCHMaterial> value : optimizedGroups) {
//            // 각 그룹의 goalWidth 값을 추출하여 출력
//            String goalWidths = value.stream()
//                    .map(coil -> String.valueOf(coil.getThickness()))  // 각 View 객체에서 goalWidth 추출
//                    .collect(Collectors.joining(", "));  // 콤마로 구분된 문자열로 변환
//
//        }

//        for(List<SCHMaterial> value : optimizedGroups) {
//            // 각 그룹의 goalWidth 값을 추출하여 출력
//            String goalWidths = value.stream()
//                    .map(coil -> String.valueOf(coil.getGoalThickness()))  // 각 View 객체에서 goalWidth 추출
//                    .collect(Collectors.joining(", "));  // 콤마로 구분된 문자열로 변환
//
//            log.info("The Group goalWidths: {}", goalWidths);
//            log.info("======================================");
//        }


        return optimizedGroups;
    }

    // 제약조건에 맞춰 미편성 처리
    private List<SCHMaterial> applyConstraintToCoils(List<SCHMaterial> coils, List<ConstraintInsertionDTO> constraintInsertionList) {
        // 미편성된 코일을 저장할 리스트
        List<SCHMaterial> unassignedCoils = new ArrayList<>();

        // thickness 제약조건을 추출
        Double thicknessConstraintValue = null;
        for (ConstraintInsertionDTO constraint : constraintInsertionList) {
            if ("thickness".equals(constraint.getTargetColumn()) && "CONSTRAINT".equals(constraint.getType())) {
                thicknessConstraintValue = Double.valueOf(constraint.getTargetValue());
                break;
            }
        }

        // 제약조건이 없으면 바로 반환
        if (thicknessConstraintValue == null) {
            return coils;  // 제약조건이 없으면 아무것도 처리하지 않고 원래 리스트 반환
        }

        // 처리된 코일을 저장할 리스트
        List<SCHMaterial> filteredCoils = new ArrayList<>(coils); // 처음엔 전체 리스트 복사

        boolean constraintViolated;

        // 리스트를 계속해서 처음부터 순회하여 제약조건 위반 코일 제거
        do {
            constraintViolated = false;  // 매 순회마다 초기화

            // 인접한 코일을 순회하면서 thickness 차이 확인
            for (int i = 1; i < filteredCoils.size(); i++) {
                SCHMaterial previousCoil = filteredCoils.get(i - 1);
                SCHMaterial currentCoil = filteredCoils.get(i);

                // 두 코일의 thickness 차이를 확인
                double thicknessDifference = Math.abs(previousCoil.getGoalThickness() - currentCoil.getGoalThickness());

                // thickness 차이가 제약조건을 넘으면 미편성 처리
                if (thicknessDifference >= thicknessConstraintValue) {
                    unassignedCoils.add(currentCoil);  // 미편성된 코일을 저장
                    filteredCoils.remove(i);  // 조건을 위반한 코일을 리스트에서 제거
                    constraintViolated = true;  // 제약조건 위반이 발생했음을 표시
                    break;  // 리스트의 처음부터 다시 검사
                }
            }
        } while (constraintViolated);  // 제약조건 위반이 발생할 때까지 반복

        // 미편성된 코일 리스트와 필터된 코일 리스트를 로그로 출력 (원하는 형식으로 출력 가능)
        System.out.println("미편성된 코일:");
        for (SCHMaterial coil : unassignedCoils) {
            System.out.println("ID: " + coil.getId() + ", Thickness: " + coil.getGoalThickness());
        }

        // 필요한 경우 미편성된 코일을 반환하거나 다른 처리 수행 가능
        // 이 예시에서는 필터된 코일 리스트를 반환
        return filteredCoils;
    }


    private List<SCHMaterial> applyPriorities(List<SCHMaterial> materials,
                                                            List<PriorityDTO> priorities) {

        List<SCHMaterial> prioritizedMaterials = new ArrayList<>();
        List<SCHMaterial> sortedMaterials = new ArrayList<>();
        List<List<SCHMaterial>> groupedMaterials = new ArrayList<>();
        for (PriorityDTO priority : priorities) {
            // 구현되면 삭제하기!
            if(priority.getId() == 5) {
                prioritizedMaterials = groupedMaterials.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                printCurrentState(prioritizedMaterials, "After applying priority: " + priority.getPriorityOrder());
                return prioritizedMaterials;
            }

            PriorityApplyMethod method = PriorityApplyMethod.valueOf(priority.getApplyMethod());
            String target = priority.getTargetColumn();
            Method getterMethod;

            try {
                getterMethod = SCHMaterial.class.getMethod("get" + convertSnakeToPascal(target));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Invalid target column: " + target, e);
            }

            switch (method) {
                case ASC:
                    groupedMaterials = sortEachGroupByThicknessAsc(groupedMaterials);
                    break;

                case DESC:
                    sortedMaterials = sortedWidthDesc(materials);
                    break;

                case GROUPING:
                    groupedMaterials = groupByWidth(sortedMaterials);
                    break;

                case CONSTRAINT:
                    break;

                case ETC:
                    groupedMaterials = applySineCurveToGroups(groupedMaterials);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown PriorityApplyMethod: " + method);
            }

            // Print the current state after applying the priority
            //printCurrentState(sortedMaterials, "After applying priority: " + priority.getPriorityOrder());
        }

        return prioritizedMaterials;
    }

    private List<SCHMaterial> groupByAndApplyNextPriority(List<SCHMaterial> materials, Method getterMethod, List<PriorityDTO> remainingPriorities) {
        // 원래 순서대로 그룹핑
        Map<Object, List<SCHMaterial>> groupedMaterials = new LinkedHashMap<>();
        for (SCHMaterial material : materials) {
            Object key = invokeGetter(material, getterMethod);
            groupedMaterials.computeIfAbsent(key, k -> new ArrayList<>()).add(material);
        }

        // 각 그룹 내에서 우선순위 적용
        List<SCHMaterial> result = new ArrayList<>();

        for (Map.Entry<Object, List<SCHMaterial>> entry : groupedMaterials.entrySet()) {
            List<SCHMaterial> group = entry.getValue();
            if(remainingPriorities.isEmpty()){
                return group;
            }
            List<SCHMaterial> sortedGroup = applyPriorities(group, remainingPriorities);
            result.addAll(sortedGroup);
        }

        return result;
    }

    private double[] generatedSineWave(int size) {
        double[] sineWave = new double[size];
        for(int i = 0; i < size; i++){
            sineWave[i] = 0.5 * Math.sin((2*Math.PI * i)/size);
        }
        return sineWave;
    }



    // Helper method to capitalize the first letter of the field name
    public String convertSnakeToPascal(String snakeCaseString) {
        // Check for null or empty input
        if (snakeCaseString == null || snakeCaseString.isEmpty()) {
            return snakeCaseString;
        }

        // Split the string by underscores
        String[] parts = snakeCaseString.split("_");

        // Convert each part to Title Case
        StringBuilder pascalCaseString = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                pascalCaseString.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }
        return pascalCaseString.toString();
    }

    // 헬퍼 메서드: Getter 메서드 호출 (제네릭)
    @SuppressWarnings("unchecked")
    private <T> T invokeGetter(Object obj, Method method) {
        try {
            return (T) method.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke getter method: " + method.getName(), e);
        }
    }

    // TODO : Cache-Server 에서 설비 가져오기!
    public List<SCHMaterial> insertMaterialsWithWorkTime(List<SCHMaterial> materials) {

        for (SCHMaterial material : materials) {
            // 작업 시간 계산
            Long workTime = calculateWorkTime(material.getGoalLength(), material.getGoalThickness(),
                    material.getGoalWidth(), material.getTotalWeight());
            //material.setWorkTime(workTime);
        }

        return materials;
    }

    // 작업 시간 계산 메서드
    // TODO: TH 설비로 계산하기
    private Long calculateWorkTime(double goalLength, double goalThickness, double goalWidth, double totalWeight) {
        return  (long) ((goalLength * goalThickness * goalWidth) / totalWeight);
    }
    private void printCurrentState(List<SCHMaterial> materials, String message) {
        log.info(message);
        for (SCHMaterial material : materials) {
            log.info("ID: {}, Goal Width: {}, Thickness: {}, Temperature: {}, RollUnit: {}",
                    material.getId(), material.getGoalWidth(), material.getGoalThickness(), material.getTemperature(), material.getRollUnit());

        }
    }
}