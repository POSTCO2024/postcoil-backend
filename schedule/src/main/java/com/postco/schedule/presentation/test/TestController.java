package com.postco.schedule.presentation.test;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.schedule.domain.test.SCHPlan;
import com.postco.schedule.service.impl.test.TestPlanAfterWorkServiceImpl;
import com.postco.schedule.service.impl.test.TestScheduleConfirmServiceImpl;
import com.postco.schedule.service.impl.test.TestSchedulePlanServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class TestController {
    private final TestSchedulePlanServiceImpl schedulePlanService;
    private final TestPlanAfterWorkServiceImpl testPlanAfterWorkService;
    private final TestScheduleConfirmServiceImpl testScheduleConfirmService;

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
     * 시퀀스 변경 및 스케쥴 확정
     */
    @PostMapping("/confirm")
    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> confirmSchedule(@RequestBody SCHForm schForm) {
        log.info("스케줄 확정 요청. 스케줄 ID: {}", schForm.getPlanId());

        return Mono.fromCallable(() -> testPlanAfterWorkService.confirmScheduleWithNewMaterials(schForm))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    if (result) {
                        // Kafka 전송
                        return Mono.fromRunnable(() -> testScheduleConfirmService.sendConfirmedSchedule(schForm.getPlanId()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .thenReturn(true);
                    }
                    return Mono.just(false);
                })
                .map(result -> ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                        .status(HttpStatus.OK.value())
                        .resultMsg("스케줄 확정 성공 및 Kafka 전송 완료")
                        .result(true)
                        .build()))
                .doOnSuccess(response -> log.info("스케줄 확정 처리 완료. 결과: {}", Objects.requireNonNull(response.getBody()).getResult()))
                .doOnError(e -> log.error("스케줄 확정 처리 중 오류 발생", e));
    }

    @GetMapping("/confirmed-results")
    public ResponseEntity<ApiResponseDTO<List<SCHConfirmDTO.View>>> getConfirmedSchedules() {

        List<SCHConfirmDTO.View> confirmedSchedules = testScheduleConfirmService.getAllConfirmedSchedules();

        ApiResponseDTO<List<SCHConfirmDTO.View>> response = ApiResponseDTO.<List<SCHConfirmDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(confirmedSchedules)
                .build();

        return ResponseEntity.ok(response);
    }

}
