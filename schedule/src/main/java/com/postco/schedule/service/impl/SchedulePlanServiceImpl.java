package com.postco.schedule.service.impl;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.SCHMaterial;
import com.postco.schedule.domain.SCHPlan;
import com.postco.schedule.domain.repository.SCHMaterialRepository;
import com.postco.schedule.domain.repository.SCHPlanRepository;
import com.postco.schedule.presentation.SCHForm;
import com.postco.schedule.presentation.dto.SCHMaterialDTO;
import com.postco.schedule.presentation.dto.SCHPlanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 스케쥴 편성 서비스
 * 저장된 스케쥴 대상재로 스케쥴 편성 진행 및 스케쥴 편성 DB 에 한꺼번에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulePlanServiceImpl {
    private final SCHMaterialRepository schMaterialRepository;
    private final SchedulingServiceImplRefac schedulingService;
//    private final SchedulingServiceImpl schedulingService;
    private final SCHPlanRepository schPlanRepository;
    private final ModelMapper modelMapper;
   // private final SchedulingServiceImplRefac testSchedule;

    // GET : fs001 Request
    public List<SCHMaterialDTO> getMaterialsByProcessCode(String processCode) {
        return MapperUtils.mapList(schMaterialRepository.findBySchPlanIsNullAndSchConfirmIsNullAndCurrProc(processCode), SCHMaterialDTO.class);
    }

    /**
     * 스케줄링 및 저장을 한번에 진행하는 메서드
     * @return List<SCHPlan> - 생성된 스케줄 목록
     */
    @Transactional
    public List<SCHPlan> executeSchedulingAndSave(List<Long> ids) {
        // step 1: ids에 해당하는 등록된 스케줄 대상재 불러오기
        // List<SCHMaterial> materials = getScheduleMaterials();
        log.info("불러온 id들 : {}", ids);
        List<SCHMaterial> materials = getScheduleMaterialsByIds(ids);
        log.info("불러온 스케쥴 대상재들 : {}", materials);
        // step 2: 공정별, 롤 단위별로 그룹화 진행
        Map<String, List<SCHMaterial>> groupedMaterials = groupedByProcessAndRollUnit(materials);
        groupedMaterials.forEach((key, value) ->
                log.info("그룹화 된 리스트: {}, Materials: {}", key, value)
        );
        // step 3: 그룹별로 스케줄링을 수행
        List<SCHMaterial> scheduledMaterials = applySchedulingByProcess(groupedMaterials);
        log.info("스케쥴링 된 후 확인 : {}", scheduledMaterials);
        // step 4: 스케줄링 완료 후 저장 진행
        return saveAllSchedules(scheduledMaterials);
    }

    // << step 1 메서드 >> : 등록된 스케쥴 대상재 모두 불러오기
    public List<SCHMaterial> getScheduleMaterials() {
        return schMaterialRepository.findAll();
    }

    // << step 1 메서드 >> : 등록된 스케쥴 대상재 필요한 id들만 불러오기
    public List<SCHMaterial> getScheduleMaterialsByIds(List<Long> ids) {
        return schMaterialRepository.findAllById(ids);
    }

    public List<SCHMaterialDTO> getScheduleMaterialsByPlanId(Long planId) {
        List<SCHMaterial> schMaterials = schMaterialRepository.findBySchPlanId(planId);

        return schMaterials.stream()
                .map(material -> {
                    SCHMaterialDTO dto = new SCHMaterialDTO();
                    dto.setId(material.getId());
                    dto.setRollUnit(material.getRollUnit());
                    dto.setCurrProc(material.getCurrProc());
                    dto.setTemperature(material.getTemperature());
                    dto.setWidth(material.getWidth());
                    dto.setThickness(material.getThickness());
                    dto.setIsScheduled(material.getIsScheduled());
                    dto.setSequence(material.getSequence());
                    dto.setIsRejected(material.getIsRejected());
                    dto.setExpectedDuration(material.getExpectedDuration());
                    dto.setWorkStatus(String.valueOf(material.getWorkStatus()));
                    dto.setGoalWidth(material.getGoalWidth());
                    dto.setGoalThickness(material.getGoalThickness());
                    dto.setNextProc(material.getNextProc());
                    dto.setMaterialNo(material.getMaterialNo());

                    // SCHPlan의 id를 schedulePlanId 필드에 매핑
                    if (material.getSchPlan() != null) {
                        dto.setSchedulePlanId(material.getSchPlan().getId());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // << step 2 메서드 >> : 공정과 롤 단위 별로 스케쥴 대상재 그룹화
    private Map<String, List<SCHMaterial>> groupedByProcessAndRollUnit(List<SCHMaterial> materials) {
        return materials.stream()
                .collect(Collectors.groupingBy(material -> material.getCurrProc() + "_" + material.getRollUnit()));
    }

    // << step 3 메서드 >> : 스케쥴링 로직을 공정과 롤 단위 별로 진행
    private List<SCHMaterial> applySchedulingByProcess(Map<String, List<SCHMaterial>> groupedMaterials) {
        List<SCHMaterial> scheduledMaterials = new ArrayList<>();
        groupedMaterials.forEach((groupKey, materials) -> {
            String processCode = materials.get(0).getCurrProc();
            log.info("그룹 키: {}, 공정 코드: {}, 재료 수: {}", groupKey, processCode, materials.size());

            // 외부 스케쥴링 서비스 호출 -> 기존의 schedulingService 활용하면 됨.
            List<SCHMaterial> scheduledProcessMaterials = schedulingService.planSchedule(materials, processCode);
            scheduledMaterials.addAll(scheduledProcessMaterials);
        });
        return scheduledMaterials;
    }

    // step 4: 편성 완료된 스케줄을 모두 저장
    @Transactional
    public List<SCHPlan> saveAllSchedules(List<SCHMaterial> scheduledMaterials) {
        // 그룹화된 데이터를 스케줄로 저장
        Map<String, List<SCHMaterial>> groupedMaterials = groupedByProcessAndRollUnit(scheduledMaterials);
        List<SCHPlan> savedPlans = new ArrayList<>();

        groupedMaterials.forEach((groupKey, groupMaterials) -> {
            // 그룹별로 스케줄 저장
            SCHPlan savedPlan = saveSchedule(groupMaterials);
            savedPlans.add(savedPlan);
        });

        return savedPlans;
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
                .materials(scheduledMaterials) // 추가 241006 by Sohyun Ahn
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

        // List<SCHPlanDTO.View>로 변환 후 반환
        return plans.stream()
                .map(plan -> modelMapper.map(plan, SCHPlanDTO.View.class))
                .collect(Collectors.toList());
    }

    // isConfirmed="N" && 지난 하루동안 편성되었던 모든 스케쥴 결과들의 id, no만 조회
    public List<SCHForm.Info> getAllScheduleNotConfirmedResults(String processCode) {
        List<SCHPlan> plans = schPlanRepository.findByProcess(processCode);

        // List<SCHForm.Info>로 변환 후 반환
        return plans.stream()
                .filter(schedulePlan -> "N".equals(schedulePlan.getIsConfirmed()))
                .filter(schedulePlan -> {
                            // planDate가 현재 날짜를 기준으로 하루 전 것부터만 보이게 구현
                            LocalDateTime today = LocalDateTime.now();
                            LocalDateTime oneDayAgo = today.minusDays(1); // 하루 전 날짜 계산
                            LocalDateTime planDate = schedulePlan.getPlanDate();
                            return planDate != null && (planDate.isAfter(oneDayAgo) || planDate.isEqual(oneDayAgo))
                                    && (planDate.isBefore(today) || planDate.isEqual(today));
                        }
                )
                .map(plan -> new SCHForm.Info(plan.getId(), plan.getScheduleNo(), plan.getScExpectedDuration()))
                .collect(Collectors.toList());
    }

}