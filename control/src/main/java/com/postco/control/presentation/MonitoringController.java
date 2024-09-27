package com.postco.control.presentation;

import com.postco.control.service.MonitoringService;
import com.postco.core.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/monitoring")
@CrossOrigin(origins = "http://localhost:4000")
@RequiredArgsConstructor
public class MonitoringController {
    private final MonitoringService monitoringService;

    @GetMapping("materials")
    public Mono<ResponseEntity<ApiResponseDTO<Map<Integer, Long>>>> filteredLocationCount() {
        return monitoringService.getMaterialsWithLocation().map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<Map<Integer, Long>>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg(HttpStatus.OK.getReasonPhrase())
                                .result(result)
                                .build()))
                .doOnError(e -> log.error("작업대상재 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<Map<Integer, Long>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("작업대상재 조회 중 오류 발생")
                                        .build())));
        
    }
}
