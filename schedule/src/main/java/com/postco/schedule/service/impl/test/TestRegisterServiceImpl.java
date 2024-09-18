package com.postco.schedule.service.impl.test;

import com.postco.core.dto.*;
import com.postco.schedule.domain.edit.SCHMaterial;
import com.postco.schedule.domain.edit.WorkStatus;
import com.postco.schedule.domain.edit.repo.SCHMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 스케쥴 대상재 관련 서비스
 * 스케쥴 대상재를 등록하고 업데이트 합니다. (예시)
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestRegisterServiceImpl {
    private final SCHMaterialRepository schMaterialRepository;

    // step1. 받아온 작업대상재 스케쥴 대상재로 등록하기
    // 2가지 방법 존재. 1) 모든 작업대상재를 스케쥴 대상재 등록 후, CAL 만 추출하여 스케쥴링 하기
    //               2) CAL 만 스케쥴 대상재로 등록하기
    // 2번째 방법으로 진행 예시.
    @Transactional
    public Mono<Void> registerScheduleMaterials(RedisDataContainer container, RefDataContainer equipmentData) {
        return Mono.fromCallable(() -> {
                    List<SCHMaterial> schMaterials = createSchMaterials(container, equipmentData);
                    return schMaterialRepository.saveAll(schMaterials);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(saved -> log.info("등록된 CAL 공정 스케줄 대상재 수: {}", saved.size()))
                .then();
    }

    // < SCHMaterial 매퍼>  : 임시 스케쥴 대상재 클래스에 대한 매퍼로,
    //                       매퍼 클래스를 따로 두거나 리팩토링 하면 좋음.
    //                       일단 급하니 빌더로 만들어 봤습니다. 스케쥴 매퍼는 별로 안길어서 그냥 사용해도 될 듯.
    private SCHMaterial convertToSCHMaterial(TargetMaterialDTO.View targetMaterial, Map<Long, MaterialDTO.View> materialMap, Map<String, EquipmentInfoDTO.View> equipmentMap) {
        // targetMaterialId를 기반으로 해당 재료 정보 찾기
        MaterialDTO.View material = materialMap.get(targetMaterial.getMaterialId());  // Long -> String 변환
        if (material == null) {
            throw new RuntimeException("해당 재료 정보를 찾을 수 없습니다: " + targetMaterial.getMaterialId());
        }

        // 재료의 현공정 값 가져오기 (currProc)
        String currProc = material.getCurrProc();

        // 현공정과 동일한 설비 데이터 가져오기 (currProc == eqCode)
        EquipmentInfoDTO.View equipment = equipmentMap.get(currProc);
        if (equipment == null) {
            throw new RuntimeException("해당 설비 데이터를 찾을 수 없습니다: " + currProc);
        }

        long expectedDuration = calculateExpectedDuration(material, equipment);

        return SCHMaterial.builder()
                .rollUnit(targetMaterial.getRollUnitName())
                .currProc(material.getCurrProc())
                .temperature(material.getTemperature())
                .width(material.getWidth())
                .thickness(material.getThickness())
                .isScheduled("N")
                .sequence(0)
                .workStatus(WorkStatus.PENDING)
                .isRejected("N")
                .expectedDuration(expectedDuration)
                .build();
    }

    // 내부 메모리 캐싱
    // SCHMaterial 생성
    private List<SCHMaterial> createSchMaterials(RedisDataContainer container, RefDataContainer equipmentData) {
        // 1. 재료 정보를 캐싱하여, materialId 기준으로 빠르게 검색할 수 있도록 Map<Long, MaterialDTO.View> 형태로 변환.
        //    이제 material.getId()를 그대로 사용하여 Long 타입의 키를 유지합니다.
        Map<Long, MaterialDTO.View> materialMap = container.getMaterials().stream()
                .collect(Collectors.toMap(MaterialDTO.View::getId, material -> material));

        // 2. 설비 정보를 캐싱하여 eqCode 기준으로 빠르게 검색할 수 있도록 Map<String, EquipmentInfoDTO.View>로 변환.
        Map<String, EquipmentInfoDTO.View> equipmentMap = equipmentData.getEquipmentInfo().stream()
                .collect(Collectors.toMap(EquipmentInfoDTO.View::getEqCode, equipment -> equipment));

        // 3. 작업대상재 중 CAL 공정 대상재만 필터링 후 SCHMaterial로 변환
        return container.getTargetMaterials().stream()
                .filter(targetMaterial -> {
                    MaterialDTO.View material = materialMap.get(targetMaterial.getMaterialId());
                    return material != null && isCALProcess(material);  // CAL 공정 필터링
                })
                .map(targetMaterial -> convertToSCHMaterial(targetMaterial, materialMap, equipmentMap))  // SCHMaterial 객체로 변환
                .collect(Collectors.toList());
    }


    //  < 내부 로직 메서드 : (1) > : 현재 공정이 CAL 인 것만 필터링하기.
    //                    현재 공정은 재료 데이터에만 존재. 재료에서 가져와서 비교해야함.
    private boolean isCALProcess(MaterialDTO.View material) {
        return material.getCurrProc() != null && material.getCurrProc().contains("CAL");
    }

    // < 내부 로직 메서드 : (2) > : 설비 데이터를 토대로 예상 작업 시간 계산하기.
    //                      설비 DTO 와 재료 DTO 를 파라미터 인자로 받음.
    //                       작업시간 = 길이 / 해당 설비 속도 (길이 값은 추후 데이터 보완 필요)
    //                       ※ 현재 DB 에 저장된 길이는 mm, 속도는 (m/min) 으로 단위 고려 계산 ※
    private long calculateExpectedDuration(MaterialDTO.View material, EquipmentInfoDTO.View equipment) {
        double lengthInMm = material.getLength(); // 재료의 길이 (mm 단위)
        double speedInMeterPerMin = equipment.getSpeed(); // 설비의 속도 (m/min 단위)

        // mm를 m로 변환
        double lengthInMeter = lengthInMm / 1000.0;

        // 작업 시간 계산 (분 단위)
        double durationInMinutes = lengthInMeter / speedInMeterPerMin;

        // 분을 초로 변환
        long durationInSeconds = Math.round(durationInMinutes * 60);

        // 최소 작업 시간 설정 (10초), 최소 시간 수정 가능.
        return Math.max(durationInSeconds, 10);
    }


    // step 3. 스케쥴 편성 후, 생기는 순서 및 미편성 여부 등을 통해
    //         스케쥴 대상재 업데이트 하기
}
