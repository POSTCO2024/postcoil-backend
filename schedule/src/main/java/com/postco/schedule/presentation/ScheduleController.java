package com.postco.schedule.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.SCHConfirm;
import com.postco.schedule.domain.SCHPlan;
import com.postco.schedule.presentation.dto.SCHMaterialDTO;
import com.postco.schedule.presentation.dto.SCHPlanDTO;
import com.postco.schedule.service.impl.PlanAfterWorkServiceImpl;
import com.postco.schedule.service.impl.ScheduleConfirmServiceImpl;
import com.postco.schedule.service.impl.SchedulePlanServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/v2/schedule") // swagger용
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000", allowCredentials = "true") // test용
public class ScheduleController {

    private final SchedulePlanServiceImpl schedulePlanService;
    private final PlanAfterWorkServiceImpl planAfterWorkService;
    private final ScheduleConfirmServiceImpl scheduleConfirmService;

    // GET : fs001 Request
    @GetMapping("/plan/{processCode}")
    public ResponseEntity<ApiResponseDTO<Page<SCHMaterialDTO>>> findAllMaterials(
            @PathVariable String processCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SCHMaterialDTO> results = schedulePlanService.getMaterialsByProcessCode(processCode, pageable);

        ApiResponseDTO<Page<SCHMaterialDTO>> response = ApiResponseDTO.<Page<SCHMaterialDTO>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // POST : fs001 Response - ScheduleMaterials 생성
    @PostMapping("/plan/execute")
    public ResponseEntity<ApiResponseDTO<List<SCHPlanDTO.View>>> executeAndSaveSchedule(@RequestBody List<Long> materialIds) {
        // 스케줄링 실행 후 결과 받기
        List<SCHPlan> savedPlans = schedulePlanService.executeSchedulingAndSave(materialIds);
        log.info("{}", savedPlans);
        // 저장된 스케줄 Plan을 DTO로 변환
        List<SCHPlanDTO.View> scheduleResults = savedPlans.stream()
                .map(plan -> new SCHPlanDTO.View(plan.getId(), plan.getScheduleNo(), plan.getProcess(),
                        plan.getRollUnit(), plan.getPlanDate(), plan.getScExpectedDuration(),
                        plan.getQuantity(), plan.getIsConfirmed(), MapperUtils.mapList(plan.getMaterials(), SCHMaterialDTO.class)))  // 재료 리스트는 필요시 추가
                .collect(Collectors.toList());
        log.info("Scheduling Results: {}", scheduleResults);

        ApiResponseDTO<List<SCHPlanDTO.View>> response = ApiResponseDTO.<List<SCHPlanDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(scheduleResults)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs002 Request
    @GetMapping("/pending/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<SCHForm.Info>>> getSchedulePlanId(@PathVariable String processCode) {
        // processCode가 오면, plan - id, scheduleNo 보내기
        List<SCHForm.Info> results = schedulePlanService.getAllScheduleNotConfirmedResults(processCode);

        ApiResponseDTO<List<SCHForm.Info>> response = ApiResponseDTO.<List<SCHForm.Info>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs002 Request2
    @GetMapping("/pending/schedule")
    public ResponseEntity<ApiResponseDTO<List<SCHMaterialDTO>>> getSchedulePlanMaterials(@RequestParam("id") Long planId) {
        List<SCHMaterialDTO> results = schedulePlanService.getScheduleMaterialsByPlanId(planId);

        ApiResponseDTO<List<SCHMaterialDTO>> response = ApiResponseDTO.<List<SCHMaterialDTO>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // POST : fs002 Request
    @PostMapping("/confirm")
    public Mono<ResponseEntity<ApiResponseDTO<List<SCHConfirm>>>> confirmScheduleForRedis(@RequestBody List<SCHForm> schForms) {
        log.info("스케줄 확정 요청. 스케줄 수: {}", schForms.size());
        return Mono.fromCallable(() -> planAfterWorkService.confirmScheduleWithNewMaterials(schForms))
                .subscribeOn(Schedulers.boundedElastic()) // 비동기 실행
                .map(results -> {
                    ApiResponseDTO<List<SCHConfirm>> response = ApiResponseDTO.<List<SCHConfirm>>builder()
                            .status(HttpStatus.OK.value())
                            .resultMsg(HttpStatus.OK.getReasonPhrase())
                            .result(results)
                            .build();

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("스케줄 확정 처리 중 오류 발생: {}", e.getMessage());

                    ApiResponseDTO<List<SCHConfirm>> errorResponse = ApiResponseDTO.<List<SCHConfirm>>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .resultMsg("스케줄 확정 중 오류가 발생했습니다.")
                            .result(Collections.emptyList())
                            .build();

                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }

    @PostMapping("/confirm/sendKafka")
    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> sendConfirmedSchedule(@RequestParam List<Long> planIds) {
        log.info("Kafka 로 스케줄 확정 전송 요청. 스케줄 IDs: {}", planIds);
        return Mono.fromRunnable(() -> scheduleConfirmService.sendConfirmedSchedule(planIds))
                .subscribeOn(Schedulers.boundedElastic())
                .then(createSuccessResponseAndLog(true, "Kafka 전송 완료", "Kafka 전송 처리"))
                .onErrorResume(e -> handleError("Kafka 전송 처리", e));
    }

    // 중복되는 응답을 하나의 메서드로 분리함
    // 다른 컨트롤러 메서드에서는 아직 적용 안함
    // 참고해서 재활용 하면 좋습니다.
    // by Yerim Kim
    private Mono<ResponseEntity<ApiResponseDTO<Boolean>>> createSuccessResponseAndLog(boolean result, String message, String operation) {
        ResponseEntity<ApiResponseDTO<Boolean>> response = ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(message)
                .result(result)
                .build());
        log.info("{} 완료. 결과: {}", operation, result);
        return Mono.just(response);
    }

    private Mono<ResponseEntity<ApiResponseDTO<Boolean>>> handleError(String operation, Throwable e) {
        log.error("{} 중 오류 발생", operation, e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<Boolean>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .resultMsg("처리 중 오류가 발생했습니다")
                        .result(false)
                        .build()));
    }
}
