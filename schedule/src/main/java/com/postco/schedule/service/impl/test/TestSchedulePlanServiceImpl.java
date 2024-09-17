package com.postco.schedule.service.impl.test;

import com.postco.schedule.domain.edit.SCHMaterial;
import com.postco.schedule.domain.edit.SCHPlan;
import com.postco.schedule.domain.edit.repo.SCHMaterialRepository;
import com.postco.schedule.domain.edit.repo.SCHPlanRepository;
import com.postco.schedule.presentation.test.SCHPlanDTO;
import com.postco.schedule.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 스케쥴 편성 서비스
 * 저장된 스케쥴 대상재로 스케쥴 편성 진행 및 스케쥴 편성 DB 에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestSchedulePlanServiceImpl {
    private final SCHMaterialRepository schMaterialRepository;
    private final SchedulingService schedulingService;
    private final SCHPlanRepository schPlanRepository;

    // 스케쥴링 및 저장을 한번에 진행하는 메서드
    @Transactional
    public List<SCHPlan> executeSchedulingAndSave() {
        // step 1: 등록된 스케줄 대상재 모두 불러오기
        List<SCHMaterial> materials = getScheduleMaterials();

        // step 2: 공정별, 롤 단위별로 그룹화 진행
        Map<String, List<SCHMaterial>> groupedMaterials = groupedByProcessAndRollUnit(materials);

        // step 3: 각 그룹에 대해 스케줄링을 진행하고 DB에 저장
        List<SCHPlan> savedPlans = new ArrayList<>();
        groupedMaterials.forEach((groupKey, groupMaterials) -> {
            log.info("그룹 키: {}, 재료 수: {}", groupKey, groupMaterials.size());

            // 스케줄 생성 및 저장
            SCHPlan savedPlan = saveSchedule(groupMaterials);
            savedPlans.add(savedPlan);
        });

        return savedPlans; // 각 그룹별로 생성된 스케줄 Plan들을 반환
    }

    // step 1: 등록된 스케쥴 대상재 모두 불러오기
    public List<SCHMaterial> getScheduleMaterials() {
        return schMaterialRepository.findAll();
    }

    // step 2: 공정과 롤 단위 별로 스케쥴 대상재 그룹화
    private Map<String, List<SCHMaterial>> groupedByProcessAndRollUnit(List<SCHMaterial> materials) {
        return materials.stream()
                .collect(Collectors.groupingBy(material -> material.getCurrProc() + "_" + material.getRollUnit()));
    }

    // step 3: 스케쥴링 로직을 공정과 롤 단위 별로 진행 (이 메서드는 외부 스케쥴링 로직을 사용)
    private List<SCHMaterial> applySchedulingByProcess(Map<String, List<SCHMaterial>> groupedMaterials) {
        List<SCHMaterial> scheduledMaterials = new ArrayList<>();
        groupedMaterials.forEach((groupKey, materials) -> {
            String processCode = materials.get(0).getCurrProc();
            log.info("그룹 키: {}, 공정 코드: {}, 재료 수: {}", groupKey, processCode, materials.size());

            // 외부 스케쥴링 서비스 호출
            List<SCHMaterial> scheduledProcessMaterials = schedulingService.testPlanSchedule(materials, processCode);
            scheduledMaterials.addAll(scheduledProcessMaterials);
        });
        return scheduledMaterials;
    }

    // 스케쥴 저장 메서드 - 단일 그룹에 대해 스케쥴을 저장
    @Transactional
    public SCHPlan saveSchedule(List<SCHMaterial> scheduledMaterials) {
        String processCode = scheduledMaterials.get(0).getCurrProc();
        String rollUnit = scheduledMaterials.get(0).getRollUnit();

        // 스케줄 Plan 생성
        SCHPlan newPlan = SCHPlan.builder()
                .scheduleNo(generateScheduleNo(processCode, rollUnit))
                .process(processCode)
                .rollUnit(rollUnit)
                .planDate(LocalDateTime.now())
                .scExpectedDuration(calculateTotalDuration(scheduledMaterials))
                .quantity(scheduledMaterials.size())
                .isConfirmed("N")
                .build();

        // 스케줄 Plan DB에 저장
        SCHPlan savedPlan = schPlanRepository.save(newPlan);

        // 재료들을 해당 스케줄에 속하도록 업데이트
        updateMaterialsWithSchedule(savedPlan, scheduledMaterials);

        return savedPlan;
    }

    // 스케쥴 대상재 업데이트 (해당 스케쥴에 소속되도록 설정)
    @Transactional
    public void updateMaterialsWithSchedule(SCHPlan plan, List<SCHMaterial> materials) {
        materials.forEach(material -> {
            material.setSchPlan(plan);   // 연관관계 설정
            material.setIsScheduled("Y");
        });

        schMaterialRepository.saveAll(materials);
    }

    // 스케쥴 번호 생성 (랜덤 No: S + 공정 이름 + 세자리 시퀀스 + 롤 단위)
    private String generateScheduleNo(String processCode, String rollUnit) {
        Long lastId = schPlanRepository.findLastSavedId();
        long nextId = (lastId != null) ? lastId + 1 : 1;
        String sequence = String.format("%03d", nextId);
        return String.format("S%s%s%s", processCode, sequence, rollUnit);
    }

    // 스케쥴 총 예상 작업시간 계산
    private Long calculateTotalDuration(List<SCHMaterial> materials) {
        return materials.stream()
                .mapToLong(SCHMaterial::getExpectedDuration)
                .sum();  // 총합 계산
    }

    // 모든 스케쥴 결과 조회
    public List<SCHPlanDTO.View> getAllScheduleResults() {
        List<SCHPlan> plans = schPlanRepository.findAll();
        ModelMapper modelMapper = new ModelMapper();

        // List<SCHPlanDTO.View>로 변환 후 반환
        return plans.stream()
                .map(plan -> modelMapper.map(plan, SCHPlanDTO.View.class))
                .collect(Collectors.toList());
    }
}