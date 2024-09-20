package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.core.dto.ApiResponseDTO;
import com.postco.control.service.DashBoardService;
import com.postco.control.service.TargetMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "http://localhost:4000")
@RequiredArgsConstructor
public class DashBoardController {
    private final DashBoardService dashBoardService;

    /**
     * 생산 마감일
     * @return
     */
    @GetMapping("/dueDate")
    public Mono<ResponseEntity<ApiResponseDTO<List<Fc004aDTO.DueDate>>>> getDueDate() {
        return Mono.just(dashBoardService.getDueDateInfo())
                .map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<List<Fc004aDTO.DueDate>>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg(HttpStatus.OK.getReasonPhrase())
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

}
