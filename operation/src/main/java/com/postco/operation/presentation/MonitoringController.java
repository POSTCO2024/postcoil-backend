package com.postco.operation.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.WorkScheduleSummaryDTO;
import com.postco.operation.service.MonitoringService;
import com.postco.operation.service.impl.work.ClientDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/operation/monitoring")
@CrossOrigin(origins = "http://localhost:4000")
@RequiredArgsConstructor
public class MonitoringController {
    @Autowired
    private final MonitoringService monitoringService;
    private final ClientDashboardService clientDashboardService;


    @GetMapping("/summary")
    public Mono<ResponseEntity<ApiResponseDTO<List<WorkScheduleSummaryDTO>>>> getWorkScheduleSummary() {
        return monitoringService.getWorkScheduleSummary()
                .map(result -> {
                    ApiResponseDTO<List<WorkScheduleSummaryDTO>> response = ApiResponseDTO.<List<WorkScheduleSummaryDTO>>builder()
                            .status(HttpStatus.OK.value())
                            .resultMsg("일일 작업 진행현황 조회 성공")
                            .result(result)
                            .build();

                    return ResponseEntity.ok(response);
                })
                .doOnError(e -> log.error("일일 작업 진행현황 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<List<WorkScheduleSummaryDTO>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("일일 작업 진행현황 조회 중 오류 발생")
                                        .build())));
    }

    @GetMapping("/analyze")
    public Mono<ResponseEntity<AnalysisDashboardClientDTO>> getFirstStatus(@RequestParam String SchProcess) {
        return clientDashboardService.sendFirstStatus(SchProcess)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
