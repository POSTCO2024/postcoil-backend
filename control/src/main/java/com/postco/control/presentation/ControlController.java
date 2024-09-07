package com.postco.control.presentation;

import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.control.presentation.dto.response.Fc002DTO;
import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.control.presentation.dto.response.TargetMaterialDTO;
import com.postco.control.service.ControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/control")
@CrossOrigin(origins = "http://localhost:4000")
public class ControlController {

    private final ControlService controlService;

    @Autowired
    public ControlController(ControlService controlService) {
        this.controlService = controlService;
    }

    /**
     * 추출 및 에러 조건에 맞는 작업 대상재 목록을 조회
     *
     * @return 조건에 맞는 Materials 리스트
     */
    @GetMapping("/target")
    public List<TargetMaterialDTO.Create> getFilteredMaterials() {
        String[] processCodes = {"1PCM", "2PCM", "1CAL", "2CAL"}; // , "1EGL", "2EGL", "1CGL", "2CGL"};
        List<TargetMaterialDTO.Create> allTargetMaterials = new ArrayList<>();

        for (String procCode : processCodes) {
            List<MaterialDTO> filteredExtraction = controlService.getFilteredExtractionMaterials(procCode);
            List<TargetMaterialDTO.Create> filteredError = controlService.extractMaterialByErrorCriteria(filteredExtraction, procCode);
            List<TargetMaterialDTO.Create> TargetMaterial = controlService.createRollUnit(filteredError, procCode);

            allTargetMaterials.addAll(TargetMaterial);
        }

        return allTargetMaterials;
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
     * @return 생성된 작업 대상재를 기준으로 정상재를 추출한 리스트
     */
    @GetMapping("/fc001a/list/{curProcCode}")
    public List<Fc001aDTO> getTargetMaterials(@PathVariable("curProcCode") String curProcCode) {
        return controlService.getNormalMaterials(curProcCode);
    }
}