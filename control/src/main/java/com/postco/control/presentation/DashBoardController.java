package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.control.service.OrderService;
import com.postco.core.dto.ApiResponseDTO;
import com.postco.control.service.DashBoardService;
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
    private final DashBoardService dashBoardService;    // Materials
    private final OrderService orderService;


    /**
     * 생산 마감일
     * @return
     */
    @GetMapping("/dueDate")
    public Mono<ResponseEntity<ApiResponseDTO<List<Fc004aDTO.DueDate>>>> getDueDate() {
        return dashBoardService.getDueDateInfo("1PCM")
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
     */
    @GetMapping("/error_count")
    public Mono<ResponseEntity<ApiResponseDTO<Fc004aDTO.ErrorCount>>> getErrorCount() {     // To do: 공정 받기
        return dashBoardService.getErrorAndNormalCount("1CAL")
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
    public Mono<Map<String, Long>> getCoilTypeCount() {   // @PathVariable String currProc
        return orderService.getCoilTypesByCurrProc("1PCM");
    }

    // 고객사
    @GetMapping("/customer_name")
    public Mono<Map<String, Long>> getCustomerCount() {
        return orderService.getCustomerCountByProc("1PCM");     // To do: 공정 받도록 수정
    }

    /**
     * 품종/고객사
     * @return
     */
    @GetMapping("/order")
    public Mono<ResponseEntity<ApiResponseDTO<Fc004aDTO.Order>>> getOrder() {
        Mono<Map<String, Long>> customerCountMono = orderService.getCustomerCountByProc("1PCM");
        Mono<Map<String, Long>> coilTypeCountMono = orderService.getCoilTypesByCurrProc("1PCM");

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

}

