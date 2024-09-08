package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.*;
import com.postco.control.service.ControlService;
import lombok.extern.slf4j.Slf4j;
import com.postco.control.service.ManagementService;
import com.postco.control.service.impl.CriteriaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/control")
@CrossOrigin(origins = "http://localhost:4000")
@Slf4j
public class ControlController {

    private final ControlService controlService;
    private final CriteriaServiceImpl criteriaService;
    private final ManagementService managementService;

    @Autowired
    public ControlController(ControlService controlService, CriteriaServiceImpl criteriaService, ManagementService managementService) {
        this.controlService = controlService;
        this.criteriaService = criteriaService;
        this.managementService = managementService;
    }

    /**
     * 추출 및 에러 조건에 맞는 작업 대상재 추출
     *
     * @return 조건에 맞는 Materials 리스트
     */
    @GetMapping("/target")
    public List<TargetMaterialDTO.Create> getFilteredMaterials() {
        String[] processCodes = {"1PCM", "2PCM", "1CAL", "2CAL"};   // 필터링 기준 - To do: 공정 추가하기 {"1EGL", "2EGL", "1CGL", "2CGL"};
        List<TargetMaterialDTO.Create> allTargetMaterials = new ArrayList<>();

        for (String procCode : processCodes) {
            List<MaterialDTO> filteredExtraction = controlService.getFilteredExtractionMaterials(procCode);
            List<TargetMaterialDTO.Create> filteredError = controlService.extractMaterialByErrorCriteria(filteredExtraction, procCode);
            List<TargetMaterialDTO.Create> TargetMaterial = controlService.createRollUnit(filteredError, procCode);

            allTargetMaterials.addAll(TargetMaterial);
        }

        return allTargetMaterials;  // To do: Result 수정
    }

    /**
     * 작업 대상재 리스트 조회
     *
     * @return 생성된 작업 대상재를 기준으로 정상재를 추출한 리스트
     */
    @GetMapping("/fc001a/list/{curProcCode}")
    public List<Fc001aDTO> getTargetMaterials(@PathVariable("curProcCode") String curProcCode) {
        return controlService.getNormalMaterials(curProcCode);
    }


    /**
     * 품종(coilTypeCode) 별 차공정(nextProc) 개수를 계산하여 표를 반환
     *
     * @return 차공정 테이블 - ArrayList<TargetMaterialDTO.Table>
     */
    @GetMapping("fc001a/table")
    public List<TargetMaterialDTO.Table> getFilteredTargetMaterials() {
        return controlService.getMaterialTable();
    }


    /**
     * 에러재를 보여주기 위한 에러재 호출
     *
     * @return 에러재 목록
     */
    @GetMapping("/error/{curProcCode}")
    public List<Fc002DTO> getErrorMaterials(@PathVariable String curProcCode) {
        List<Fc002DTO> erorrMaterialList = controlService.getErrorMaterials(curProcCode);
        return erorrMaterialList;
    }

    /**
     * 에러 패스를 통해 에러재를 정상재로 변환한다.
     * 에러 여부(isError)는 업데이트 되며 에러이유(errorType)은 그대로 둔다.
     * 다시 에러재로 추출될 경우, 에러이유는 업데이트 된다.
     *
     * @return
     */
    @PutMapping("/errorpass")
    public List<Long> ErrorPass(@RequestBody List<Long> error_material_ids) {
        log.info("에러패스를 진행합니다. " + error_material_ids);

        controlService.errorPass(error_material_ids);

        return error_material_ids;  // return 수정
    }


    @GetMapping("/management/extraction/{processcode}")
    public CriteriaDTO getExtractionStandard(@PathVariable String processcode) {
        return criteriaService.findExtractionCriteriaByProcessCode(processcode);
    }

    @GetMapping("/management/error/{processcode}")
    public CriteriaDTO getErrorStandard(@PathVariable String processcode) {
        return criteriaService.findErrorCriteriaByProcessCode(processcode);
    }

    @PostMapping("/management/extraction/{processcode}")
    public void postExtractionStandard(@RequestBody ExtractionStandardDTO extractionStandardDTO, @PathVariable String processcode) {
        controlService.updateExtractStandard(extractionStandardDTO, processcode);
    }

    @PostMapping("/management/error/{processcode}")
    public CriteriaDTO postErrorStandard(@RequestBody ErrorStandardDTO errorStandardDTO, @PathVariable String processcode) {
        System.out.println(errorStandardDTO.toString());
        managementService.updateErrorStandard(errorStandardDTO, processcode);
        return criteriaService.findErrorCriteriaByProcessCode(processcode);
    }
}