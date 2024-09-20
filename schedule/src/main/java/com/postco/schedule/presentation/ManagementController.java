package com.postco.schedule.presentation;

import com.postco.core.dto.ApiResponseDTO;
import com.postco.schedule.presentation.dto.ManagementDTO;
import com.postco.schedule.service.impl.ManagementServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management/schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000", allowCredentials = "true") // test용
public class ManagementController {

    private final ManagementServiceImpl managementService;

    @GetMapping("/{processCode}/{rollUnit}")
    public ResponseEntity<ApiResponseDTO<ManagementDTO>> getManagementDataByProcessAndUnit(
            @PathVariable String processCode,
            @PathVariable String rollUnit) {

        ManagementDTO results = managementService.findManagementDataByProcessCodeAndRollUnit(processCode, rollUnit);

        ApiResponseDTO<ManagementDTO> response = ApiResponseDTO.<ManagementDTO>builder()
                .status(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .result(results)
                .build();

        return ResponseEntity.ok(response);
    }
}
