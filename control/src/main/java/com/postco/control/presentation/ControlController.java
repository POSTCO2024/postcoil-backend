package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.*;
import com.postco.control.service.ControlService;
import com.postco.control.service.impl.CriteriaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4000")
@RestController
@RequestMapping("/control")
public class ControlController {

    private final ControlService controlService;
    private final CriteriaServiceImpl criteriaService;

    @Autowired
    public ControlController(ControlService controlService, CriteriaServiceImpl criteriaService) {
        this.controlService = controlService;
        this.criteriaService = criteriaService;
    }

    /**
     * 추출 및 에러 조건에 맞는 작업 대상재 목록을 조회
     *
     * @return 조건에 맞는 Materials 리스트
     */
    @GetMapping("/target")
    public List<TargetMaterialDTO.Create> getFilteredMaterials() {
        List<MaterialDTO> filteredExtraction = controlService.getFilteredExtractionMaterials();
        List<TargetMaterialDTO.Create> filteredError = controlService.extractMaterialByErrorCriteria(filteredExtraction);
        List<TargetMaterialDTO.Create> TargetMaterial = controlService.createRollUnit(filteredError);

        return TargetMaterial;
    }

    /**
     * 에러재를 보여주기 위한 에러재 호출
     *
     * @return
     */
    @GetMapping("/error")
    public List<Fc002DTO> getErrorMaterials() {
        List<Fc002DTO> erorrMaterialList = controlService.getErrorMaterials();
        return erorrMaterialList;
    }

    /**
     * 작업 대상재 리스트 조회
     *
     * @return 생성된 작업 대상재를 기준으로 정상재를 추출한 리스
     */
    @GetMapping("/fc001a")
    public List<Fc001aDTO> getTargetMaterials() {
        return controlService.getNormalMaterials();
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
}