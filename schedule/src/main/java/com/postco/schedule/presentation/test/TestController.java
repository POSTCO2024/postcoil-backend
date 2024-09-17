package com.postco.schedule.presentation.test;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.schedule.domain.edit.SCHMaterial;
import com.postco.schedule.domain.edit.SCHPlan;
import com.postco.schedule.service.impl.test.TestSchedulePlanServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class TestController {
    private final TestSchedulePlanServiceImpl schedulePlanService;

    /**
     * 스케줄링을 진행하고 저장하는 메서드
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponseDTO<List<SCHPlanDTO.View>>> executeAndSaveSchedule() {
        // 스케줄링 실행 후 결과 받기
        List<SCHPlan> savedPlans = schedulePlanService.executeSchedulingAndSave();

        // 저장된 스케줄 Plan을 DTO로 변환
        List<SCHPlanDTO.View> scheduleResults = savedPlans.stream()
                .map(plan -> new SCHPlanDTO.View(plan.getId(), plan.getScheduleNo(), plan.getProcess(),
                        plan.getRollUnit(), plan.getPlanDate(), plan.getScExpectedDuration(),
                        plan.getQuantity(), plan.getIsConfirmed(), null))  // 재료 리스트는 필요시 추가
                .collect(Collectors.toList());

        // ApiResponseDTO로 wrapping 해서 반환
        ApiResponseDTO<List<SCHPlanDTO.View>> response = ApiResponseDTO.<List<SCHPlanDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(scheduleResults)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 모든 스케줄 결과 조회 메서드
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponseDTO<List<SCHPlanDTO.View>>> getAllScheduleResults() {
        // 저장된 모든 스케줄 결과 조회
        List<SCHPlanDTO.View> results = schedulePlanService.getAllScheduleResults();

        // ApiResponseDTO로 wrapping 해서 반환
        ApiResponseDTO<List<SCHPlanDTO.View>> response = ApiResponseDTO.<List<SCHPlanDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 스케줄링만 진행하는 메서드 (저장 없이 스케줄 결과만 반환)
     */
    @GetMapping("/doSCH")
    public ResponseEntity<ApiResponseDTO<List<SCHMaterial>>> executeScheduling() {
        // 스케줄링 실행 후 결과 받기 (저장 없이)
        List<SCHMaterial> scheduledMaterials = schedulePlanService.getScheduleMaterials();  // 혹은 스케줄링 적용 로직이 필요하면 수정

        // ApiResponseDTO로 결과 반환
        ApiResponseDTO<List<SCHMaterial>> response = ApiResponseDTO.<List<SCHMaterial>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(scheduledMaterials)
                .build();

        return ResponseEntity.ok(response);
    }

}
