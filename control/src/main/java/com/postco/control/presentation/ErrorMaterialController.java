package com.postco.control.presentation;

import com.postco.control.domain.ErrorMaterialMapper;
import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.control.service.ErrorPassService;
import com.postco.control.service.impl.ErrorMaterialQueryServiceImpl;
import com.postco.core.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4000")
@RequestMapping("/api/v1/error-materials")
@RequiredArgsConstructor
public class ErrorMaterialController {
    private final ErrorMaterialQueryServiceImpl errorMaterialQueryService;
    private final ErrorPassService errorPassService;

    /**
     * 공정 별 에러재 조회
     *
     * @param currProc
     * @return 공정 별 에러재 리스트
     */
    @GetMapping("/error-by-curr-proc")
    public Mono<ResponseEntity<ApiResponseDTO<List<TargetViewDTO>>>> getNormalTargetMaterialsByCurrProc(
            @RequestParam String currProc) {
        return errorMaterialQueryService.getErrorMaterialsByCurrProc(currProc)
                .map(result -> ResponseEntity.ok(
                        ApiResponseDTO.<List<TargetViewDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .resultMsg(HttpStatus.OK.getReasonPhrase())
                                .result(result)
                                .build()))
                .doOnError(e -> log.error("에러재 조회 중 오류 발생", e))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponseDTO.<List<TargetViewDTO>>builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .resultMsg("에러재 조회 중 오류 발생")
                                        .build())));
    }

    /**
     * 에러 패스를 통해 에러재를 정상재로 변환한다.
     * 에러 여부(isError)는 업데이트 되며 에러이유(errorType)은 그대로 둔다.
     * 다시 에러재로 추출될 경우, 에러이유는 업데이트 된다.
     *
     * @return
     * @Param 에러패스 할 재료(material_id)
     */
    @PutMapping("/errorpass")
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> errorPass(@RequestBody List<Long> error_material_ids) {
        log.info("에러패스를 진행합니다. " + error_material_ids);
        errorPassService.errorPass(error_material_ids);

        Map<String, Long> result = ErrorMaterialMapper.errorPassIds(error_material_ids);
        ApiResponseDTO<Map<String, Long>> response = new ApiResponseDTO<>(200, "Success", result);

        return ResponseEntity.ok(response);
    }

}
