package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.PriorityApplyMethod;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.presentation.dto.PriorityDTO;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    // TODO: Repository -> Redis 에서 불러오기1
    private final ScheduleMaterialsRepository scheduleMaterialsRepository;
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;

    private final PriorityService priorityService;
    private final ConstraintInsertionService constraintInsertionService;

    public List<ScheduleMaterialsDTO.View> executeScheduling(List<Long> materialIds, String processCode) {
        List<ScheduleMaterialsDTO.View> materials = MapperUtils.mapList(scheduleMaterialsRepository.findAllById(materialIds), ScheduleMaterialsDTO.View.class);
        List<PriorityDTO> priorities = priorityService.findAllByProcessCode(processCode);
        // TODO: Repository -> Redis 에서 불러오기2
        /* Redis */
        // Redis에서 데이터 가져오기 (예: "materials" 키에 저장된 데이터)
//        List<ScheduleMaterialsDTO.View> materials = materialIds.stream()
//                .map(id -> (ScheduleMaterialsDTO.View) redisTemplate.opsForValue().get("material:" + id))
//                .collect(Collectors.toList());

        // 우선순위 적용
        List<ScheduleMaterialsDTO.View> sortedMaterials = applyPriorities(materials, priorities);

        // priorityOrder 설정
        for (int i = 0; i < sortedMaterials.size(); i++) {
            sortedMaterials.get(i).setProcessOrder(Collections.singletonList(i + 1));
        }

        return sortedMaterials;
    }

    private void printCurrentState(List<ScheduleMaterialsDTO.View> materials, String message) {
        System.out.println(message);
        for (ScheduleMaterialsDTO.View material : materials) {
            System.out.printf("ID: %d, Target Width: %.1f, Thickness: %.1f%n",
                    material.getId(), material.getTargetWidth(), material.getThickness());
        }
        System.out.println();
    }

    private List<ScheduleMaterialsDTO.View> applyPriorities(List<ScheduleMaterialsDTO.View> materials, List<PriorityDTO> priorities) {
        List<ScheduleMaterialsDTO.View> sortedMaterials = new ArrayList<>(materials);

        for (PriorityDTO priority : priorities) {
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
                    printCurrentState(sortedMaterials, "After applying priority: " + priority.getPriorityOrder());
                    return sortedMaterials; // GROUPING의 경우, 그룹 내에서 다시 적용된 결과를 반환

                case CONSTRAINT:
                    // Implement custom logic for CONSTRAINT
                    break;

                case ETC:
                    // Implement custom logic for ETC
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
            List<ScheduleMaterialsDTO.View> sortedGroup = applyRemainingPriorities(group, remainingPriorities);
            result.addAll(sortedGroup);
        }

        return result;
    }

    private List<ScheduleMaterialsDTO.View> applyRemainingPriorities(List<ScheduleMaterialsDTO.View> materials, List<PriorityDTO> priorities) {
        if (priorities.isEmpty()) {
            return materials;
        }

        return applyPriorities(materials, priorities);
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

    // GROUPING .length > 1 경우에
//    private List<ScheduleMaterials> getScheduleMaterials(ScheduleMaterials scheduleMaterials){
//
//    }



}
