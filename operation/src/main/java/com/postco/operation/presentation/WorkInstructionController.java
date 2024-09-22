package com.postco.operation.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.service.WorkInstructionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/work-instructions")
@RequiredArgsConstructor
@Slf4j
public class WorkInstructionController {
    private final WorkInstructionService workInstructionService;

    @GetMapping
    public Mono<ResponseEntity<ApiResponseDTO<List<WorkInstructionDTO.View>>>> getWorkInstructions(
            @RequestParam String process,
            @RequestParam String rollUnit) {
        log.info("작업 지시서 조회 요청. 공정: {}, 롤 단위: {}", process, rollUnit);
        return workInstructionService.getWorkInstructions(process, rollUnit)
                .flatMap(result -> createSuccessResponseAndLog(result, "작업 지시서 조회 성공", "작업 지시서 조회"))
                .onErrorResume(e -> handleError("작업 지시서 조회", e));
    }

    private <T> Mono<ResponseEntity<ApiResponseDTO<T>>> createSuccessResponseAndLog(T result, String message, String operation) {
        ResponseEntity<ApiResponseDTO<T>> response = ResponseEntity.ok(ApiResponseDTO.<T>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(message)
                .result(result)
                .build());
        log.info("{} 완료. 결과: {}", operation, result);
        return Mono.just(response);
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
