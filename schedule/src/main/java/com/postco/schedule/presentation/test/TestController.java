package com.postco.schedule.presentation.test;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.schedule.domain.edit.SCHMaterial;
import com.postco.schedule.service.impl.test.TestSchedulePlanServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class TestController {
    private final TestSchedulePlanServiceImpl schedulePlanService;

    /**
     * 스케줄링 실행 엔드포인트
     * 이 엔드포인트를 호출하면 스케줄링이 실행되고 결과가 로그로 출력됩니다.
     */
    @GetMapping("/execute")
    public ResponseEntity<ApiResponseDTO<List<SCHMaterial>>> executeScheduling() {
        // 스케줄링 실행 후 결과 받기
        List<SCHMaterial> scheduledMaterials = schedulePlanService.executeScheduling();

        // ApiResponseDTO로 결과 반환
        ApiResponseDTO<List<SCHMaterial>> response = ApiResponseDTO.<List<SCHMaterial>>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(scheduledMaterials)
                .build();

        return ResponseEntity.ok(response);
    }

}
