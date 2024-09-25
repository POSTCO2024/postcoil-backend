package com.postco.schedule.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.schedule.domain.SCHPlan;
import com.postco.schedule.presentation.dto.SCHConfirmDTO;
import com.postco.schedule.presentation.dto.SCHMaterialDTO;
import com.postco.schedule.presentation.dto.SCHPlanDTO;
import com.postco.schedule.service.impl.PlanAfterWorkServiceImpl;
import com.postco.schedule.service.impl.ScheduleConfirmServiceImpl;
import com.postco.schedule.service.impl.SchedulePlanServiceImpl;
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
@RequestMapping("api/v2/schedule") // swagger용
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000", allowCredentials = "true") // test용
public class ScheduleController {

    private final SchedulePlanServiceImpl schedulePlanService;
    private final PlanAfterWorkServiceImpl planAfterWorkService;
    private final ScheduleConfirmServiceImpl scheduleConfirmService;

    // GET : fs001 Request
    @GetMapping("/plan/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<SCHMaterialDTO>>> findAllMaterials(@PathVariable String processCode){
        List<SCHMaterialDTO> results = schedulePlanService.getMaterialsByProcessCode(processCode);

        ApiResponseDTO<List<SCHMaterialDTO>> response = ApiResponseDTO.<List<SCHMaterialDTO>>builder()
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
                        plan.getQuantity(), plan.getIsConfirmed(), null))  // 재료 리스트는 필요시 추가
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
    // TODO: 예림 언니가 변경한 코드 보기~!
//    @PostMapping("/confirm")
//    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> confirmSchedule(@RequestBody SCHForm schForm) {
//        log.info("스케줄 확정 요청. 스케줄 ID: {}", schForm.getPlanId());
//
//        return Mono.fromCallable(() -> planAfterWorkService.confirmScheduleWithNewMaterials(schForm))
//                .subscribeOn(Schedulers.boundedElastic())
//                .flatMap(result -> {
//                    if (result) {
//                        // Kafka 전송
//                        return Mono.fromRunnable(() -> scheduleConfirmService.sendConfirmedSchedule(schForm.getPlanId()))
//                                .subscribeOn(Schedulers.boundedElastic())
//                                .thenReturn(true);
//                    }
//                    return Mono.just(false);
//                })
//                .map(result -> ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
//                        .status(HttpStatus.OK.value())
//                        .resultMsg("스케줄 확정 성공 및 Kafka 전송 완료")
//                        .result(true)
//                        .build()))
//                .doOnSuccess(response -> log.info("스케줄 확정 처리 완료. 결과: {}", Objects.requireNonNull(response.getBody()).getResult()))
//                .doOnError(e -> log.error("스케줄 확정 처리 중 오류 발생", e));
//    }

    // GET : fs003 Request
    @GetMapping("/result/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<SCHForm.Info>>> getScheduleResultsId(@PathVariable String processCode) {
        // processCode가 오면, confirm - id, scheduleNo 보내기
        List<SCHForm.Info> results = scheduleConfirmService.getAllConfirmedScheduleIdsFromInProgressToPending(processCode);

        ApiResponseDTO<List<SCHForm.Info>> response = ApiResponseDTO.<List<SCHForm.Info>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs003 Request2
    @GetMapping("/result/schedule")
    public ResponseEntity<ApiResponseDTO<List<SCHMaterialDTO>>> getScheduleResultMaterials(@RequestParam("id") Long confirmId) {
        List<SCHMaterialDTO> results = scheduleConfirmService.getScheduleMaterialsByConfirmId(confirmId);

        ApiResponseDTO<List<SCHMaterialDTO>> response = ApiResponseDTO.<List<SCHMaterialDTO>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // TODO: fs003 WebSocket 통신 / (작업완료, 작업대기, 보급대기, 보급완료) 어떻게 보는지

    // GET : fs004 Request
    @GetMapping("/timeline/{processCode}")
    public ResponseEntity<ApiResponseDTO<List<SCHConfirmDTO.View>>> getScheduleResultsIdByDates(@PathVariable String processCode, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        List<SCHConfirmDTO.View> confirmedSchedules = scheduleConfirmService.getAllConfirmedSchedulesBetweenDates(startDate, endDate, processCode);

        ApiResponseDTO<List<SCHConfirmDTO.View>> response = ApiResponseDTO.<List<SCHConfirmDTO.View>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(confirmedSchedules)
                .build();

        return ResponseEntity.ok(response);
    }

    // GET : fs004 Request2
    @GetMapping("/timeline/schedule")
    public ResponseEntity<ApiResponseDTO<List<SCHMaterialDTO>>> getScheduleMaterials(@RequestParam("id") Long confirmId) {
        List<SCHMaterialDTO> results = scheduleConfirmService.getScheduleMaterialsByConfirmId(confirmId);

        ApiResponseDTO<List<SCHMaterialDTO>> response = ApiResponseDTO.<List<SCHMaterialDTO>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }

    // ============================ 테스트용 controller ================================

    @PostMapping("/confirm")
    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> confirmScheduleForRedis(@RequestBody List<SCHForm> schForms) {
        log.info("스케줄 확정 요청. 스케줄 수: {}", schForms.size());
        return Mono.fromCallable(() -> planAfterWorkService.confirmScheduleWithNewMaterials(schForms))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> createSuccessResponseAndLog(result, result ? "스케줄 확정 성공" : "스케줄 확정 실패", "스케줄 확정 처리"))
                .onErrorResume(e -> handleError("스케줄 확정 처리", e));
    }

    @PostMapping("/confirm/sendKafka")
    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> sendConfirmedSchedule(@RequestParam List<Long> planIds) {
        log.info("Kafka 로 스케줄 확정 전송 요청. 스케줄 IDs: {}", planIds);
        return Mono.fromRunnable(() -> scheduleConfirmService.sendConfirmedSchedule(planIds))
                .subscribeOn(Schedulers.boundedElastic())
                .then(createSuccessResponseAndLog(true, "Kafka 전송 완료", "Kafka 전송 처리"))
                .onErrorResume(e -> handleError("Kafka 전송 처리", e));
    }

    // =================================================================


    // 중복되는 응답을 하나의 메서드로 분리함
    // 다른 컨트롤러 메서드에서는 아직 적용 안함
    // 참고해서 재활용 하면 좋습니다.
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
