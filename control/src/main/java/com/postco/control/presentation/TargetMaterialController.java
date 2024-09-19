package com.postco.control.presentation;

import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.control.service.TargetMaterialService;
import com.postco.control.service.impl.TargetMaterialQueryServiceImpl;
import com.postco.core.dto.ApiResponseDTO;
import com.postco.core.dto.RedisDataContainer;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@RestController
@RequestMapping("/api/v1/target-materials")
@RequiredArgsConstructor
public class TargetMaterialController {
    private final TargetMaterialQueryServiceImpl targetMaterialQueryService;

    @GetMapping("/related-data")
    public Mono<ResponseEntity<ApiResponseDTO<List<TargetViewDTO>>>> getTargetMaterialsWithRelatedData() {
        return targetMaterialQueryService.mapToTargetViewDTOs()
                .map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<List<TargetViewDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg(HttpStatus.OK.getReasonPhrase())
                                .result(result)
                                .build()))
                .doOnError(e -> log.error("작업대상재 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<List<TargetViewDTO>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("작업대상재 조회 중 오류 발생")
                                        .build())));
    }

    @GetMapping("/normal-by-curr-proc")
    public Mono<ResponseEntity<ApiResponseDTO<List<TargetViewDTO>>>> getNormalTargetMaterialsByCurrProc(
            @RequestParam String currProc) {
        return targetMaterialQueryService.getNormalMaterialsByCurrProc(currProc)
                .map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<List<TargetViewDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg(HttpStatus.OK.getReasonPhrase())
                                .result(result)
                                .build()))
                .doOnError(e -> log.error("정상재 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<List<TargetViewDTO>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("정상재 조회 중 오류 발생")
                                        .build())));
    }

}
