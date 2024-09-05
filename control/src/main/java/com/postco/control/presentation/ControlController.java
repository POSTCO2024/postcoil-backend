package com.postco.control.presentation;

import com.postco.control.domain.Materials;
import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.control.presentation.dto.response.TargetMaterialDTO;
import com.postco.control.service.ControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.List;

@RestController
@RequestMapping("/control")
public class ControlController {

    private final ControlService controlService;

    @Autowired
    public ControlController(ControlService controlService) {
        this.controlService = controlService;
    }

    /**
     * 추출 및 에러 조건에 맞는 작업 대상재 목록을 조회
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
     * 작업 대상재 리스트 조회
     * @return 생성된 작업 대상재를 기준으로 정상재를 추출한 리스
     */
    @GetMapping("/fc001a")
    public List<Fc001aDTO> getTargetMaterials() {
        return controlService.getMaterials();
    }
}