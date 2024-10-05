package com.postco.operation.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
import com.postco.operation.service.WorkInstructionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v2/work-instructions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4000", "http://localhost:8084"}, allowCredentials = "true") // test용
@Slf4j
public class WorkInstructionController {
    private final WorkInstructionService workInstructionService;
    
    // 삭제 Sohyun Ahn 241001
    @GetMapping("/pending-schedule")
    public Mono<ResponseEntity<ApiResponseDTO<List<WorkInstructionDTO.SimulationView>>>> getWorkInstructions() {
        return workInstructionService.getUncompletedWorkInstructionsForSimulation()
                .flatMap(result -> createSuccessResponseAndLog(result, "작업 지시서 조회 성공", "작업 지시서 조회"))
                .onErrorResume(e -> handleError("작업 지시서 조회", e));
    }

    /*
     * 추가 Sohyun Ahn 240930,
     */
    @GetMapping("/uncompleted")
    public Mono<ResponseEntity<ApiResponseDTO<List<ClientDTO>>>> getWorkInstructionsBeforeWebSocket(
            @RequestParam String process) {
        log.info("작업 지시서 및 현재 진행 상황 통계 조회 요청. 공정: {}", process);
        return workInstructionService.getUncompletedWorkInstructionsBeforeWebSocket(process)
                .flatMap(clientDTO -> createSuccessResponseAndLog(clientDTO, "작업 지시서 및 현재 진행 상황 통계 조회 성공", "작업 지시서 조회"))
                .onErrorResume(e -> handleError("작업 지시서 및 현재 진행 상황 통계 조회", e));
    }

    @GetMapping("/completed")
    public Mono<ResponseEntity<ApiResponseDTO<List<WorkInstructionDTO.View>>>> getWorkInstructionsExceptFinished(
            @RequestParam("process") String process, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        log.info("작업 지시서 조회 요청. 공정: {}, 롤 단위: {}", process);
        return workInstructionService.getCompletedWorkInstructions(process, startDate, endDate)
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
