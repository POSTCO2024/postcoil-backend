package com.postco.schedule.service;

import com.postco.schedule.domain.ScheduleConfirm;
import com.postco.schedule.domain.SchedulePlan;
import com.postco.schedule.domain.repository.SchedulePlanRepository;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataInsertService {

    private final SchedulePlanRepository schedulePlanRepository; // DB 접근을 위한 리포지토리

    // TODO : Cache-Server 에서 설비 가져오기!
    public List<ScheduleMaterialsDTO.View> insertExpectedItemDuration(List<ScheduleMaterialsDTO.View> materials) {

        for (ScheduleMaterialsDTO.View material : materials) {
            // 작업 시간 계산
            Long expectedItemDuration = calculateExpectedItemDuration(material.getGoalLength(), material.getGoalThickness(),
                    material.getGoalWidth(), material.getTotalWeight());
            material.setExpectedItemDuration(expectedItemDuration);
        }

        return materials;
    }

    // 작업 시간 계산 메서드
    // TODO: TH 설비로 계산하기
    private Long calculateExpectedItemDuration(double goalLength, double goalThickness, double goalWidth, double totalWeight) {
        return  (long) ((goalLength * goalThickness * goalWidth) / totalWeight);
    }

    // 스케줄no 생성 메서드
    public String createScheduleNo(String processCode, ScheduleMaterialsDTO.View firstMaterial) {
        String rollUnit = firstMaterial.getRollUnitName();

        // 랜덤 문자 2자리 생성
        String randomLetters = generateRandomLetters(2);

        // DB에서 가장 큰 schedule_no 조회
        Integer maxScheduleNo = schedulePlanRepository.findMaxScheduleNoByProcessCodeAndRollUnit(processCode, rollUnit);

        // 가장 큰 schedule_no에서 숫자 부분 추출
        int nextSequenceNumber = (maxScheduleNo == null) ? 1 : maxScheduleNo + 1;

        // 4자리로 포맷팅된 순차 증가 숫자 생성
        String sequentialNumber = String.format("%04d", nextSequenceNumber);

        // 최종 schedule_no 생성
        return "S" + processCode + randomLetters + sequentialNumber + rollUnit;
    }

    // 랜덤 문자 2자리 생성 메소드
    private String generateRandomLetters(int length) {
        Random random = new Random();
        StringBuilder letters = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char randomChar = (char) ('A' + random.nextInt(26)); // A~Z 중 랜덤 선택
            letters.append(randomChar);
        }

        return letters.toString();
    }

    // sch_plan db 삽입
    public SchedulePlan createSchedulePlan(List<ScheduleMaterialsDTO.View> materials, String processCode) {
        SchedulePlan schedulePlan = new SchedulePlan();
        schedulePlan.setMaterials(materials);
        schedulePlan.setProcessCode(processCode);
        schedulePlan.setPlanDate(LocalDateTime.now());
        schedulePlan.setIsConfirmed("N");
        schedulePlan.setNo(createScheduleNo(processCode, materials.get(0)));
        schedulePlan.setMaterialIds(schedulePlan.getMaterials()
                .stream()
                .map(ScheduleMaterialsDTO.View::getId) // Extract ID from each material DTO
                .collect(Collectors.toList()));
        schedulePlan.setQuantity((long) materials.size());

        return schedulePlan;
    }

    // sch_confirm db 삽입
    public ScheduleConfirm createScheduleConfirm(SchedulePlan schedulePlan) {
        ScheduleConfirm scheduleConfirm = new ScheduleConfirm();
        scheduleConfirm.setScheduleId(schedulePlan.getId());
        scheduleConfirm.setScheduleNo(schedulePlan.getNo());
        scheduleConfirm.setProcessCode(schedulePlan.getProcessCode());
        scheduleConfirm.setConfirmDate(LocalDateTime.now());

        return scheduleConfirm;
    }
}
