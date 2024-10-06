package com.postco.operation.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import com.postco.operation.presentation.dto.WorkScheduleSummaryDTO;
import com.postco.operation.service.WorkItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v2/work-instructions-items")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4000", "http://localhost:8084"}, allowCredentials = "true") // test용
@Slf4j
public class WorkInstructionItemController {
    private final WorkItemService workItemService;
    @GetMapping("/get-items")
    public Mono<ResponseEntity<ApiResponseDTO<List<WorkInstructionItemDTO.SimulationItemDTO>>>> getWorkInstructions(
            @RequestParam Long workInstructionId) {

        // 비동기적으로 처리
        return workItemService.getWorkItems(workInstructionId)
                .map(result -> {
                    ApiResponseDTO<List<WorkInstructionItemDTO.SimulationItemDTO>> response =
                            ApiResponseDTO.<List<WorkInstructionItemDTO.SimulationItemDTO>>builder()
                                    .status(HttpStatus.OK.value())
                                    .resultMsg("아이템 전송 성공")
                                    .result(result)
                                    .build();
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.<List<WorkInstructionItemDTO.SimulationItemDTO>>builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .resultMsg("해당 작업 지시서에 대한 아이템이 없습니다.")
                                .result(null)
                                .build()
                        ));
    }
}