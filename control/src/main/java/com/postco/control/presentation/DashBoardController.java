package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.control.service.DashBoardOrderService;
import com.postco.core.dto.ApiResponseDTO;
import com.postco.control.service.DashBoardMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "http://localhost:4000")
@RequiredArgsConstructor
public class DashBoardController {
    private final DashBoardMaterialService dashBoardMaterialService;    // Materials
    private final DashBoardOrderService dashBoardOrderService;


    /**
     * 생산 마감일
     * @return 공정 별 생산 마감일이 적게 남은 재료 리스트 반환
     */
    @GetMapping("/dueDate")
    public Mono<ResponseEntity<ApiResponseDTO<List<Fc004aDTO.DueDate>>>> getDueDate(@RequestParam String currProc) {
        return dashBoardMaterialService.getDueDateInfo(currProc)
                .map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<List<Fc004aDTO.DueDate>>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg("생산 기한일 조회 성공")
                                .result(result)
                                .build()))
                .doOnError(e -> log.error("생산 기한일 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<List<Fc004aDTO.DueDate>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("생산 기한일 조회 중 오류 발생")
                                        .build())));
    }

    /**
     * 에러재/정상재 비율
     * @return 공정 별 에러재/정상재 개수 반환
     */
    @GetMapping("/error_count")
    public Mono<ResponseEntity<ApiResponseDTO<Fc004aDTO.ErrorCount>>> getErrorCount(@RequestParam String currProc) {
        return dashBoardMaterialService.getErrorAndNormalCount(currProc)
                .flatMap(result -> Mono.just(
                        ResponseEntity.ok(
                                ApiResponseDTO.<Fc004aDTO.ErrorCount>builder()
                                        .status(HttpStatus.OK.value())
                                        .resultMsg("에러재/정상재 비율 조회 성공")
                                        .result(result)
                                        .build()
                        )
                ))
                .doOnError(e -> log.error("에러재/정상재 비율 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<Fc004aDTO.ErrorCount>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("에러재/정상재 비율 조회 중 오류 발생")
                                        .build()
                                )
                ));
    }



    // 품종
    @GetMapping("/coil_type")
    public Mono<Map<String, Long>> getCoilTypeCount(@RequestParam String currProc) {
        return dashBoardOrderService.getCoilTypesByCurrProc(currProc);
    }

    // 고객사
    @GetMapping("/customer_name")
    public Mono<Map<String, Long>> getCustomerCount(@RequestParam String currProc) {
        return dashBoardOrderService.getCustomerCountByProc(currProc);
    }

    /**
     * 품종/고객사
     * @return 공정 별 품종 및 고객사 개수
     */
    @GetMapping("/order")
    public Mono<ResponseEntity<ApiResponseDTO<Fc004aDTO.Order>>> getOrder(@RequestParam String currProc) {
        Mono<Map<String, Long>> customerCountMono = dashBoardOrderService.getCustomerCountByProc(currProc);
        Mono<Map<String, Long>> coilTypeCountMono = dashBoardOrderService.getCoilTypesByCurrProc(currProc);

        return Mono.zip(customerCountMono, coilTypeCountMono)
                .flatMap(this::buildOrderResponse)  // 빌드된 응답을 반환
                .onErrorResume(e -> {
                    log.error("Order 정보 조회 중 오류 발생", e);
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(ApiResponseDTO.<Fc004aDTO.Order>builder()
                                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                            .resultMsg("Order 정보 조회 중 오류 발생")
                                            .build()));
                });
    }

    private Mono<ResponseEntity<ApiResponseDTO<Fc004aDTO.Order>>> buildOrderResponse(Tuple2<Map<String, Long>, Map<String, Long>> tuple) {
        Fc004aDTO.Order order = Fc004aDTO.Order.builder()
                .customerName(tuple.getT1())  // customerCount를 매핑
                .coilType(tuple.getT2())      // coilType을 매핑
                .build();

        ApiResponseDTO<Fc004aDTO.Order> apiResponse = ApiResponseDTO.<Fc004aDTO.Order>builder()
                .status(HttpStatus.OK.value())
                .resultMsg("품종 및 고객사 비율 조회 성공")
                .result(order)
                .build();

        return Mono.just(ResponseEntity.ok(apiResponse));
    }


    /**
     * 재료정보(폭,두께 분포)
     * @return 공정 별 작업대상재의 폭, 두께 분포
     */
    @GetMapping("/distribution")
    public Mono<ResponseEntity<ApiResponseDTO<Fc004aDTO.WidthThicknessCount>>> getMaterialDistribution(@RequestParam String currProc) {
        return dashBoardMaterialService.getWidthAndThicknessDistribution(currProc)
                .map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<Fc004aDTO.WidthThicknessCount>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg("폭 및 두께 분포 데이터 조회 성공")
                                .result(result)
                                .build()))
                .doOnError(e -> log.error("폭 및 두께 분포 조회 중 오류 발생"))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<Fc004aDTO.WidthThicknessCount>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("폭 및 두께 분포 조회 중 오류 발생")
                                        .build())));
    }

    /**
     * 공정 별 롤단위 비율
     *
     */
    @GetMapping("/rollUnit")
    public Mono<ResponseEntity<ApiResponseDTO<Fc004aDTO.RollUnitCount>>> getRollUnit(@RequestParam String currProc) {
        return dashBoardMaterialService.getRollUnitCountByCurrProc(currProc)
                .map(rollUnitCount -> ResponseEntity.ok(
                        ApiResponseDTO.<Fc004aDTO.RollUnitCount>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg("롤 단위 카운트 조회 성공")
                                .result(rollUnitCount) // 롤 단위 카운트 결과를 설정
                                .build()))
                .doOnError(e -> log.error("롤 단위 카운트 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<Fc004aDTO.RollUnitCount>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("롤 단위 카운트 조회 중 오류 발생")
                                        .build())));
    }

}

