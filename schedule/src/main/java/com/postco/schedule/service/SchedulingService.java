package com.postco.schedule.service;

import com.postco.schedule.domain.PriorityApplyMethod;
import com.postco.schedule.domain.test.SCHMaterial;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.presentation.dto.PriorityDTO;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
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
public class SchedulingService {

    private final ScheduleMaterialsRepository scheduleMaterialsRepository;

    private final PriorityService priorityService;
    private final ConstraintInsertionService constraintInsertionService;

    private final double STANDARD_WIDTH = 50;

    // *************** 임의로 수정한 스케쥴링 진행 호출 메서드 ************
    // 테스트 용. 폭 기준 내림차순 함수만 가지고 적용 예정 ( 대충 전체 스케쥴링 메서드라고 가정)
    public List<SCHMaterial> testPlanSchedule(List<SCHMaterial> materials, String processCode) {
        // 현재 저장된 우선순위 없음 -> 패스
        List<PriorityDTO> priorities = priorityService.findAllByProcessCode(processCode);

        // 우선순위 적용
        List<SCHMaterial> sortedMaterials = testApplyPriorities(materials, priorities);

        // priorityOrder 설정
        for (int i = 0; i < sortedMaterials.size(); i++) {
            sortedMaterials.get(i).setSequence(i + 1);
        }

        return sortedMaterials;
    }



    // 우선순위 적용 함수
    private List<SCHMaterial> testApplyPriorities(List<SCHMaterial> materials, List<PriorityDTO> priorities) {
        // 여기서는 간단히 폭 기준 내림차순만 적용
        return testSortedWidthDesc(materials);
    }



    // 폭 기준 내림차순 함수
    // 그외 등등 기존 작성 우선순위 함수들 이런식으로 수정해서 적용하면 됨.
    private List<SCHMaterial> testSortedWidthDesc(List<SCHMaterial> materials) {
        return materials.stream()
                .sorted(Comparator.comparingDouble(SCHMaterial::getWidth).reversed())
                .collect(Collectors.toList());
    }

    // *****************************************************************//



    //* 스케줄링로직~! */
    public List<ScheduleMaterialsDTO.View> planSchedule(List<ScheduleMaterialsDTO.View> materials, String processCode) {
        List<PriorityDTO> priorities = priorityService.findAllByProcessCode(processCode);

        // 우선순위 적용
        List<ScheduleMaterialsDTO.View> sortedMaterials = applyPriorities(materials, priorities);

        // priorityOrder 설정
        for (int i = 0; i < sortedMaterials.size(); i++) {
            sortedMaterials.get(i).setSequence(Collections.singletonList(i + 1));
        }

        return sortedMaterials;
    }

    // 폭 기준 내림차순 함수
    private List<ScheduleMaterialsDTO.View> sortedWidthDesc(List<ScheduleMaterialsDTO.View> coils){
        List<ScheduleMaterialsDTO.View> result = new ArrayList<>();
        result= coils.stream()
                .sorted(Comparator.comparingDouble(ScheduleMaterialsDTO.View::getGoalWidth).reversed())
                .collect(Collectors.toList());


        return result;
    }


    // 폭 기준 동일폭 기준에 맞춰 그룹핑
    private List<List<ScheduleMaterialsDTO.View>> groupByWidth(List<ScheduleMaterialsDTO.View> sortedCoils){
        List<List<ScheduleMaterialsDTO.View>> coilGroups = new ArrayList<>();
        List<ScheduleMaterialsDTO.View> currentCoilGroup = new ArrayList<>();

        // 첫번째 코일 기준 start
        ScheduleMaterialsDTO.View firstCoil = sortedCoils.get(0);
        currentCoilGroup.add(firstCoil);
        double currentBaseWidth = firstCoil.getGoalWidth();

        // 그룹핑 로직
        for(int i = 1; i < sortedCoils.size(); i++){
            ScheduleMaterialsDTO.View currentCoil = sortedCoils.get(i);
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

        for(List<ScheduleMaterialsDTO.View> value : coilGroups) {
            // 각 그룹의 goalWidth 값을 추출하여 출력
            String goalWidths = value.stream()
                    .map(coil -> String.valueOf(coil.getGoalWidth()))  // 각 View 객체에서 goalWidth 추출
                    .collect(Collectors.joining(", "));  // 콤마로 구분된 문자열로 변환


        }

        return coilGroups;
    }

    // 폭 기준 동일폭 그룹들을 각각 두께 오름차순
    private List<List<ScheduleMaterialsDTO.View>> sortEachGroupByThicknessAsc
    (List<List<ScheduleMaterialsDTO.View>> groupCoils) {

        List<List<ScheduleMaterialsDTO.View>> result = new ArrayList<>();

        result = groupCoils.stream()
                .map(group -> group.stream()
                        .sorted(Comparator.comparingDouble(ScheduleMaterialsDTO.View::getThickness))
                        .collect(Collectors.toList())
                ).collect(Collectors.toList());
        for(List<ScheduleMaterialsDTO.View> value : result) {
            // 각 그룹의 goalWidth 값을 추출하여 출력
            String goalWidths = value.stream()
                    .map(coil -> String.valueOf(coil.getThickness()))  // 각 View 객체에서 goalWidth 추출
                    .collect(Collectors.joining(", "));  // 콤마로 구분된 문자열로 변환


        }


        return result;
    }

    // 각 그룹들 sin 그래프로 배치
    private List<List<ScheduleMaterialsDTO.View>> applySineCurveToGroups
    (List<List<ScheduleMaterialsDTO.View>> groupCoils){

        List<List<ScheduleMaterialsDTO.View>> optimizedGroups = new ArrayList<>();
        List<Double> prevGroupLastThickness = new ArrayList<>();
        prevGroupLastThickness.add(0.0); // 초기값 설정

        for(List<ScheduleMaterialsDTO.View> group : groupCoils){
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
            List<ScheduleMaterialsDTO.View> optimizedGroup = new ArrayList<>();
            for (int i = 0; i < group.size(); i++) {
                optimizedGroup.add(group.get(sineIndices.get(i)));
            }

            // 마지막 코일 두께를 리스트로 업데이트
            prevGroupLastThickness.set(0, optimizedGroup.get(optimizedGroup.size() - 1).getThickness());

            optimizedGroups.add(optimizedGroup);
        }

        for(List<ScheduleMaterialsDTO.View> value : optimizedGroups) {
            // 각 그룹의 goalWidth 값을 추출하여 출력
            String goalWidths = value.stream()
                    .map(coil -> String.valueOf(coil.getThickness()))  // 각 View 객체에서 goalWidth 추출
                    .collect(Collectors.joining(", "));  // 콤마로 구분된 문자열로 변환

        }


        return optimizedGroups;
    }



    private List<ScheduleMaterialsDTO.View> applyPriorities(List<ScheduleMaterialsDTO.View> materials,
                                                            List<PriorityDTO> priorities) {

        List<ScheduleMaterialsDTO.View> prioritizedMaterials = new ArrayList<>();
        List<ScheduleMaterialsDTO.View> sortedMaterials = new ArrayList<>();
        List<List<ScheduleMaterialsDTO.View>> groupedMaterials = new ArrayList<>();
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
                getterMethod = ScheduleMaterialsDTO.View.class.getMethod("get" + convertSnakeToPascal(target));
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

        }

        return prioritizedMaterials;
    }

    private List<ScheduleMaterialsDTO.View> groupByAndApplyNextPriority(List<ScheduleMaterialsDTO.View> materials, Method getterMethod, List<PriorityDTO> remainingPriorities) {
        // 원래 순서대로 그룹핑
        Map<Object, List<ScheduleMaterialsDTO.View>> groupedMaterials = new LinkedHashMap<>();
        for (ScheduleMaterialsDTO.View material : materials) {
            Object key = invokeGetter(material, getterMethod);
            groupedMaterials.computeIfAbsent(key, k -> new ArrayList<>()).add(material);
        }

        // 각 그룹 내에서 우선순위 적용
        List<ScheduleMaterialsDTO.View> result = new ArrayList<>();

        for (Map.Entry<Object, List<ScheduleMaterialsDTO.View>> entry : groupedMaterials.entrySet()) {
            List<ScheduleMaterialsDTO.View> group = entry.getValue();
            if(remainingPriorities.isEmpty()){
                return group;
            }
            List<ScheduleMaterialsDTO.View> sortedGroup = applyPriorities(group, remainingPriorities);
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

    private void printCurrentState(List<ScheduleMaterialsDTO.View> materials, String message) {
        log.info(message);
        for (ScheduleMaterialsDTO.View material : materials) {
            log.info("ID: {}, Goal Width: {}, Thickness: {}, Temperature: {}, CoilTypeCode: {}",
                    material.getId(), material.getGoalWidth(), material.getThickness(), material.getTemperature(), material.getCoilTypeCode());

        }
    }
}