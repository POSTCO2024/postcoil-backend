package com.postco.operation.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.operation.service.CoilSupplyService;
import com.postco.operation.service.CoilWorkCommandService;
import com.postco.operation.service.WorkItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coil-work")
@Slf4j
public class WorkController {
    private final CoilWorkCommandService coilWorkCommandService;
    private final CoilSupplyService coilSupplyService;
    private final WorkItemService workItemService;

    // 보급 요구 API
    @PostMapping("/request-supply/{workInstructionId}")
    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> requestSupply(
            @PathVariable Long workInstructionId,
            @RequestParam int supplyCount) {
        log.info("보급 요청. 작업 지시서 ID: {}, 보급 수량: {}", workInstructionId, supplyCount);

        return coilWorkCommandService.requestSupply(workInstructionId, supplyCount)
                .map(success -> createResponse(success,
                        success ? "보급 요청 성공" : "보급 요청 실패"))
                .onErrorResume(e -> handleError("보급 요청 중 오류 발생", e));
    }

    // 코일 Reject API
    @PostMapping("/reject/{workInstructionId}/{itemId}")
    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> rejectWorkItem(
            @PathVariable Long workInstructionId,
            @PathVariable Long itemId) {
        log.debug("리젝 요청. 작업 지시서 ID: {}, 작업 아이템 ID: {}", workInstructionId, itemId);
        return Mono.fromCallable(() -> {
                    boolean coilUpdateResult = coilSupplyService.updateRejectCount(workInstructionId);
                    boolean workItemUpdateResult = workItemService.rejectWorkItem(itemId);
                    return coilUpdateResult && workItemUpdateResult;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(success -> createResponse(success,
                        success ? "Reject 처리가 완료되었습니다." : "Reject 처리 중 일부 오류가 발생했습니다."))
                .doOnNext(response ->
                        log.info("Reject 처리 {}: 작업 지시서 ID: {}, 작업 아이템 ID: {}",
                                Objects.requireNonNull(response.getBody()).getResult()
                                        ? "코일 Reject 성공" : "코일 Reject 실패", workInstructionId, itemId))
                .onErrorResume(e -> handleError("리젝 처리 중 오류 발생", e));
    }

    // 긴급 정지 API



    private ResponseEntity<ApiResponseDTO<Boolean>> createResponse(boolean success, String message) {
        return ResponseEntity.ok(ApiResponseDTO.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(message)
                .result(success)
                .build());
    }

    private <T> Mono<ResponseEntity<ApiResponseDTO<T>>> handleError(String operation, Throwable e) {
        log.error("{} 중 오류 발생", operation, e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.<T>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .resultMsg("처리 중 오류가 발생했습니다")
                        .result(null)
                        .build()));
    }
}
