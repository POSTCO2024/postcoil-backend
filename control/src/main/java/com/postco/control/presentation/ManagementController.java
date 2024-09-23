package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.CriteriaDTO;
import com.postco.control.presentation.dto.response.ErrorStandardDTO;
import com.postco.control.presentation.dto.response.ExtractionStandardDTO;
import com.postco.control.service.ManagementService;
import com.postco.control.service.impl.CriteriaServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/management")
@CrossOrigin(origins = "http://localhost:4000")
@RequiredArgsConstructor
public class ManagementController {
    private final CriteriaServiceImpl criteriaService;
    private final ManagementService managementService;

    /**
     * 기준 관리
     */
    @GetMapping("/extraction/{processcode}")
    public CriteriaDTO getExtractionStandard(@PathVariable String processcode) {
        return criteriaService.findExtractionCriteriaByProcessCode(processcode);
    }

    @GetMapping("/error/{processcode}")
    public CriteriaDTO getErrorStandard(@PathVariable String processcode) {
        return criteriaService.findErrorCriteriaByProcessCode(processcode);
    }

    @PostMapping("/extraction/{processcode}")
    public void postExtractionStandard(@RequestBody ExtractionStandardDTO extractionStandardDTO, @PathVariable String processcode) {
        managementService.updateExtractStandard(extractionStandardDTO, processcode);
    }

    @PostMapping("/error/{processcode}")
    public CriteriaDTO postErrorStandard(@RequestBody ErrorStandardDTO errorStandardDTO, @PathVariable String processcode) {
        System.out.println(errorStandardDTO.toString());
        managementService.updateErrorStandard(errorStandardDTO, processcode);
        return criteriaService.findErrorCriteriaByProcessCode(processcode);
    }
}
