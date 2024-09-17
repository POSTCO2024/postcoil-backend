package com.postco.schedule.service.impl.test;

import com.postco.schedule.domain.edit.SCHMaterial;
import com.postco.schedule.domain.edit.repo.SCHMaterialRepository;
import com.postco.schedule.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // 롤 단위 별 스케쥴링 진행
    public List<SCHMaterial> executeScheduling() {
        // step1. 등록된 스케줄 대상재 모두 불러오기 (CAL 만 저장했음)
        List<SCHMaterial> materials = getScheduleMaterials();


        // step2. 롤 단위 별 그룹화 진행 (rollUnitName)
        Map<String, List<SCHMaterial>> groupedMaterials = groupedByRollUnitName(materials);

        // step4. 스케줄링 로직 진행
        List<SCHMaterial> scheduledMaterials = new ArrayList<>();
        groupedMaterials.forEach((rollUnit, rollUnitMaterials) -> {
            log.info("롤 유닛: {}, 재료 수: {}", rollUnit, rollUnitMaterials.size());

            // 공정별 그룹화 및 스케줄링 로직 적용
            List<SCHMaterial> scheduledRollUnitMaterials = applySchedulingByProcess(rollUnitMaterials);
            scheduledMaterials.addAll(scheduledRollUnitMaterials);
        });

        scheduledMaterials.forEach(material -> log.info("스케쥴링 결과: {}", material));

        // step4. 하나의 스케쥴로 편성

        return scheduledMaterials;
    }


    // step1. 등록된 스케쥴 대상재 모두 불러오기 (CAL 만 저장했음)
    //        ( 모든 작업대상재 저장했으면 currProc 가 CAL 인 것만 조회 필요 )
    public List<SCHMaterial> getScheduleMaterials() {
        return schMaterialRepository.findAll();
    }

    // step2. 롤 단위 별 그룹화 진행 (rollUnitName)
    // 여기서 공정 별 그룹화를 한꺼번에 진행해도 됨. 지금은 어떤 순서로 되는 지 몰라서 분리함.
    // 기존에 존재하는 함수명 동일 사용. materialsGroupedByRollUnitName
    private Map<String, List<SCHMaterial>> groupedByRollUnitName(List<SCHMaterial> materials) {
        return materials.stream()
                .collect(Collectors.groupingBy(SCHMaterial::getRollUnit));
    }


    private Map<String, List<SCHMaterial>> groupByProcessCode(List<SCHMaterial> materials) {
        return materials.stream()
                .collect(Collectors.groupingBy(SCHMaterial::getCurrProc));  // 공정 코드 기준으로 그룹화
    }

    // step3. 스케쥴링 로직 진행 ( 동일한 공정끼리 )
    // 기존의 planSchedule 호출( Yerim kim 이 수정한 버전의 메서드 호출했음)
    private List<SCHMaterial> applySchedulingByProcess(List<SCHMaterial> groupedMaterial) {
        List<SCHMaterial> scheduledMaterials = new ArrayList<>();

        // 공정별로 그룹화
        Map<String, List<SCHMaterial>> materialsByProcess = groupByProcessCode(groupedMaterial);

        // 공정별로 스케쥴링 로직 적용
        materialsByProcess.forEach((processCode, processMaterials) -> {
            log.info("공정 코드: {}, 재료 수: {}", processCode, processMaterials.size());

            // 스케쥴링 로직 호출
            List<SCHMaterial> scheduledProcessMaterials = schedulingService.testPlanSchedule(processMaterials, processCode);
            scheduledMaterials.addAll(scheduledProcessMaterials);
        });

        return scheduledMaterials;
    }



    // 하나의 스케쥴로 편성




    // 스케쥴 대상재 저장
    // 1) 스케쥴 plan DB 에 저장하면서 + 2) 스케쥴 대상 재료 값도 업데이트 해야함.


    // step 1. 스케쥴 편성 DB 에 저장
    //         스케쥴 랜덤 No, 해당 공정, 스케쥴 총 예상 작업 시간(각 코일 예상 작업 시간 합), 코일 수, 컨펌 여부


    // step 2. 스케쥴 대상 재료 업데이트
    //         편성된 재료는 isScheduled 를 Y 로 속한 스케쥴 schedulePlanId 업데이트, sequence 업데이트 필요





}
