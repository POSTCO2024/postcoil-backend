package com.postco.control.presentation;

import com.postco.control.domain.ErrorMaterialMapper;
import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.control.service.ErrorPassService;
import com.postco.control.service.SearchMaterialService;
import com.postco.control.service.impl.ErrorMaterialQueryServiceImpl;
import com.postco.core.dto.ApiResponseDTO;
import com.postco.websocket.service.CoilService;
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
@CrossOrigin(origins = {"http://localhost:4000", "http://localhost:8081"})
@RequestMapping("/api/v1/control/error-materials")
@RequiredArgsConstructor
public class ErrorMaterialController {
    private final ErrorMaterialQueryServiceImpl errorMaterialQueryService;
    private final ErrorPassService errorPassService;
    private final SearchMaterialService searchMaterialService;
    private final CoilService coilService;


    /**
     * 공정 별 에러재 조회
     *
     * @param currProc
     * @return 공정 별 에러재 리스트
     */
//    @GetMapping("/related-data")
//    public Mono<ResponseEntity<ApiResponseDTO<List<TargetViewDTO>>>> getTargetMaterialsWithRelatedData() {
//        return errorMaterialQueryService.mapToTargetViewDTOs()
//                .map(result -> ResponseEntity.ok(
//                        ApiResponseDTO.<List<TargetViewDTO>>builder()
//                                .status(HttpStatus.OK.value())
//                                .resultMsg(HttpStatus.OK.getReasonPhrase())
//                                .result(result)
//                                .build()))
//                .doOnError(e -> log.error("작업대상재 조회 중 오류 발생", e))
//                .onErrorResume(e -> Mono.just(
//                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                .body(ApiResponseDTO.<List<TargetViewDTO>>builder()
//                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                                        .resultMsg("작업대상재 조회 중 오류 발생")
//                                        .build())));
//    }
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
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> ErrorPass(@RequestBody List<Long> error_material_ids) {
        log.info("에러패스를 진행합니다. " + error_material_ids);
        errorPassService.errorPass(error_material_ids);

        Map<String, Long> result = ErrorMaterialMapper.errorPassIds(error_material_ids);
        ApiResponseDTO<Map<String, Long>> response = new ApiResponseDTO<>(200, "Success", result);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/comment/{id}")
    public ResponseEntity<ApiResponseDTO<String>> errorComment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String comment = request.get("comment");
        String materialNo = request.get("materialNo");
        System.out.println(comment);
        errorPassService.errorComment(id, comment);
        ApiResponseDTO<String> response = new ApiResponseDTO<>(200, "에러재 코멘트 성공", "true");
        log.info("에러재 코멘트 : {}", "코멘트가 등록되었습니다");
        coilService.directMessageToClient("/topic/errorMessage", request);
        return ResponseEntity.ok(response);
    }

}
