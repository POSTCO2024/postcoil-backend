package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.PriorityApplyMethod;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.presentation.dto.PriorityDTO;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {

    private final ScheduleMaterialsRepository scheduleMaterialsRepository;

    private final PriorityService priorityService;
    private final ConstraintInsertionService constraintInsertionService;

    //* 스케줄링로직~! */
    public List<ScheduleMaterialsDTO.View> planSchedule(List<Long> materialIds, String processCode) {
        // TODO: Redis Cache-server에서 List<ScheduleMaterialsDTO.Target>로 materialIds에 해당하는 재료들 불러오기
        //  scheduleMaterialsRepository.findAllById(materialIds) -> (cache에서 가져 온) materials로 변경
        List<ScheduleMaterialsDTO.View> materials = MapperUtils.mapList(scheduleMaterialsRepository.findAllById(materialIds), ScheduleMaterialsDTO.View.class); // 나중에 삭제하기
        List<PriorityDTO> priorities = priorityService.findAllByProcessCode(processCode);
        // TODO: Constraints 추가하기

        // 우선순위 적용
        List<ScheduleMaterialsDTO.View> sortedMaterials = applyPriorities(materials, priorities);

        // priorityOrder 설정
        for (int i = 0; i < sortedMaterials.size(); i++) {
            sortedMaterials.get(i).setSequence(Collections.singletonList(i + 1));
        }

        return sortedMaterials;
    }


    private void printCurrentState(List<ScheduleMaterialsDTO.View> materials, String message) {
        log.info(message);
        for (ScheduleMaterialsDTO.View material : materials) {
            log.info("ID: {}, Goal Width: {}, Thickness: {}, Temperature: {}, CoilTypeCode: {}",
                    material.getId(), material.getGoalWidth(), material.getThickness(), material.getTemperature(), material.getCoilTypeCode());

        }
    }

    private List<ScheduleMaterialsDTO.View> applyPriorities(List<ScheduleMaterialsDTO.View> materials, List<PriorityDTO> priorities) {
        List<ScheduleMaterialsDTO.View> sortedMaterials = new ArrayList<>(materials);

        for (PriorityDTO priority : priorities) {
            // 구현되면 삭제하기!
            if(priority.getId() == 6){
                printCurrentState(sortedMaterials, "After applying priority: " + priority.getPriorityOrder());

                return sortedMaterials;
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
                    sortedMaterials.sort(Comparator.comparing(material -> invokeGetter(material, getterMethod)));
                    break;

                case DESC:
                    sortedMaterials.sort(Comparator.comparing(material -> invokeGetter(material, getterMethod)).reversed());
                    break;

                case GROUPING:
                    // Handle potential out-of-bounds exceptions
                    int startIndex = priorities.indexOf(priority) + 1;
                    int endIndex = priorities.size();
                    if (startIndex < endIndex) {
                        List<PriorityDTO> remainingPriorities = priorities.subList(startIndex, endIndex);
                        sortedMaterials = groupByAndApplyNextPriority(sortedMaterials, getterMethod, remainingPriorities);
                    }
                    printCurrentState(sortedMaterials, "After applying priority: " + priority.getPriorityOrder() + priority.getName());
                    return sortedMaterials; // GROUPING의 경우, 그룹 내에서 다시 적용된 결과를 반환

                case CONSTRAINT:
                    // Implement custom logic for CONSTRAINT
                    break;

                case ETC:

                    break;

                default:
                    throw new IllegalArgumentException("Unknown PriorityApplyMethod: " + method);
            }

            // Print the current state after applying the priority
            printCurrentState(sortedMaterials, "After applying priority: " + priority.getPriorityOrder());
        }

        return sortedMaterials;
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
    public List<ScheduleMaterialsDTO.View> insertMaterialsWithWorkTime(List<ScheduleMaterialsDTO.View> materials) {

        for (ScheduleMaterialsDTO.View material : materials) {
            // 작업 시간 계산
            Long workTime = calculateWorkTime(material.getGoalLength(), material.getGoalThickness(),
                    material.getGoalWidth(), material.getTotalWeight());
            material.setWorkTime(workTime);
        }

        return materials;
    }

    // 작업 시간 계산 메서드
    // TODO: TH 설비로 계산하기
    private Long calculateWorkTime(double goalLength, double goalThickness, double goalWidth, double totalWeight) {
        return  (long) ((goalLength * goalThickness * goalWidth) / totalWeight);
    }
}
