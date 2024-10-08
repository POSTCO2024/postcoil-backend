package com.postco.control.presentation;

import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.control.service.NextProcessQueryService;
import com.postco.control.service.SearchMaterialService;
import com.postco.control.service.impl.TargetMaterialQueryServiceImpl;
import com.postco.core.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/control/target-materials")
@CrossOrigin(origins = {"http://localhost:4000", "http://localhost:8081"})
@RequiredArgsConstructor
public class TargetMaterialController {
    private final TargetMaterialQueryServiceImpl targetMaterialQueryService;
    private final NextProcessQueryService nextProcessQueryService;
    private final SearchMaterialService searchMaterialService;

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

    /**
     * 품종(coilTypeCode) 별 차공정(nextProc) 개수를 계산하여 표를 반환
     *
     * @return 차공정 테이블 - ArrayList<TargetMaterialDTO.Table>
     */

    @GetMapping("/nextProcTable")
    public Mono<ResponseEntity<ApiResponseDTO<List<Fc001aDTO.Table>>>> getMaterialTable(@RequestParam String currProc) {
        return nextProcessQueryService.getMaterialTable(currProc)
                .map(result -> {
                    ApiResponseDTO<List<Fc001aDTO.Table>> responseDTO = ApiResponseDTO.<List<Fc001aDTO.Table>>builder()
                            .status(HttpStatus.OK.value())
                            .resultMsg("Material Table 조회 완료")
                            .result(result)
                            .build();

                    return ResponseEntity.ok(responseDTO);
                })
                .doOnError(e -> log.error("Material Table 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<List<Fc001aDTO.Table>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("Material Table 조회 중 오류 발생")
                                        .build())
                ));
    }

    /**
     * 검색
     * @param currProc: 공정
     * @param searchCriteria: 검색 기준
     * @param searchValue: 검색 값 (keyword)
     * @param minValue: 검색 범위 (min)
     * @param maxValue: 검색 범위 (max)
     * @return 검색이 완료된 리스트 반환
     */
    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponseDTO<List<TargetViewDTO>>>> searchTargetMaterials(
            @RequestParam String currProc,
            @RequestParam(required = false) String searchCriteria, // 검색 조건
            @RequestParam(required = false) String searchValue, // 키워드 검색 값
            @RequestParam(required = false) String minValue,    // 범위 최소 값
            @RequestParam(required = false) String maxValue,    // 범위 최대 값
            @RequestParam(required = false) String isError) {   // 에러 여부

        
        log.debug("[검색정보] 선택 공정: "+ currProc +
                "  검색 기준: " + searchCriteria +
                "  키워드: " + searchValue +
                "  범위: " + minValue + " ~ " + maxValue);

        return searchMaterialService.searchMaterialsByCurrProc(currProc, searchCriteria, searchValue, minValue, maxValue, isError)
                .map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<List<TargetViewDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg("검색 완료")
                                .result(result)
                                .build()))
                .doOnError(e -> log.error("검색 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<List<TargetViewDTO>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("검색 중 오류 발생")
                                        .build())));
    }

}
