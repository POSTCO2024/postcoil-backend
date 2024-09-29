package com.postco.control.presentation;

//import com.postco.control.presentation.dto.response.*;
//import com.postco.control.service.ControlService;
import com.postco.control.service.ErrorPassService;
//import com.postco.control.service.ManagementService;
//import com.postco.control.service.impl.CriteriaServiceImpl;
//import com.postco.core.dto.TargetMaterialDTO;
import com.postco.core.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.View;
import com.postco.control.domain.ErrorMaterialMapper;

//import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/control")
@CrossOrigin(origins = "http://localhost:4000")
@RequiredArgsConstructor
public class ControlController {

//    private final ControlService controlService;
//    private final CriteriaServiceImpl criteriaService;
//    private final ManagementService managementService;
    private final ErrorPassService errorPassService;

//    @Autowired
//    public ControlController(CriteriaServiceImpl criteriaService, ManagementService managementService, ErrorPassService errorPassService, View error) {
////        this.controlService = controlService;
//        this.criteriaService = criteriaService;
//        this.managementService = managementService;
//        this.errorPassService = errorPassService;
//        this.error = error;
//    }

//    /**
//     * 추출 및 에러 조건에 맞는 작업 대상재 추출
//     *
//     * @return 조건에 맞는 Materials 리스트
//     */
//    @GetMapping("/target")
//    public List<TargetMaterialDTO.Create> getFilteredMaterials() {
//        String[] processCodes = {"1PCM", "2PCM", "1CAL", "2CAL"};   // 필터링 기준 - To do: 공정 추가하기 {"1EGL", "2EGL", "1CGL", "2CGL"};
//        List<TargetMaterialDTO.Create> allTargetMaterials = new ArrayList<>();
//
//        for (String procCode : processCodes) {
//            List<MaterialDTO> filteredExtraction = controlService.getFilteredExtractionMaterials(procCode);
//            List<TargetMaterialDTO.Create> filteredError = controlService.extractMaterialByErrorCriteria(filteredExtraction, procCode);
//            List<TargetMaterialDTO.Create> TargetMaterial = controlService.createRollUnit(filteredError, procCode);
//
//            allTargetMaterials.addAll(TargetMaterial);
//        }
//
//        return allTargetMaterials;  // To do: Result 수정
//    }
//
//    /**
//     * 작업 대상재 리스트 조회
//     *
//     * @return 생성된 작업 대상재를 기준으로 정상재를 추출한 리스트
//     */
//    @GetMapping("/fc001a/list/{curProcCode}")
//    public List<Fc001aDTO> getTargetMaterials(@PathVariable("curProcCode") String curProcCode) {
//        return controlService.getNormalMaterials(curProcCode);
//    }
//
//    /**
//     * 품종(coilTypeCode) 별 차공정(nextProc) 개수를 계산하여 표를 반환
//     *
//     * @return 차공정 테이블 - ArrayList<TargetMaterialDTO.Table>
//     */
//    @GetMapping("fc001a/table")
//    public List<TargetMaterialDTO.Table> getFilteredTargetMaterials() {
//        return controlService.getMaterialTable();
//    }
//
//    /**
//     * 에러재를 보여주기 위한 에러재 호출
//     *
//     * @return
//     */
//    @GetMapping("/error/{curProcCode}")
//    public List<Fc002DTO> getErrorMaterials(@PathVariable("curProcCode") String curProcCode) {
//        List<Fc002DTO> erorrMaterialList = controlService.getErrorMaterials(curProcCode);
//
//        return erorrMaterialList;

//    /**
//     * 에러 패스를 통해 에러재를 정상재로 변환한다.
//     * 에러 여부(isError)는 업데이트 되며 에러이유(errorType)은 그대로 둔다.
//     * 다시 에러재로 추출될 경우, 에러이유는 업데이트 된다.
//     *
//     * @Param 에러패스 할 재료(material_id)
//     * @return
//     */
//    @PutMapping("/errorpass")
//    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> ErrorPass(@RequestBody List<Long> error_material_ids) {
//        log.info("에러패스를 진행합니다. " + error_material_ids);
//        errorPassService.errorPass(error_material_ids);
//
//        Map<String, Long> result = ErrorMaterialMapper.errorPassIds(error_material_ids);
//        ApiResponseDTO<Map<String, Long>> response = new ApiResponseDTO<>(200,"Success",result);
//
//        return ResponseEntity.ok(response);
//    }


//    /**
//     * 기준 관리
//     */
//    @GetMapping("/management/extraction/{processcode}")
//    public CriteriaDTO getExtractionStandard(@PathVariable String processcode) {
//        return criteriaService.findExtractionCriteriaByProcessCode(processcode);
//    }
//
//    @GetMapping("/management/error/{processcode}")
//    public CriteriaDTO getErrorStandard(@PathVariable String processcode) {
//        return criteriaService.findErrorCriteriaByProcessCode(processcode);
//    }
//
//    @PostMapping("/management/extraction/{processcode}")
//    public void postExtractionStandard(@RequestBody ExtractionStandardDTO extractionStandardDTO, @PathVariable String processcode) {
//        controlService.updateExtractStandard(extractionStandardDTO, processcode);
//    }
//
//    @PostMapping("/management/error/{processcode}")
//    public CriteriaDTO postErrorStandard(@RequestBody ErrorStandardDTO errorStandardDTO, @PathVariable String processcode) {
//        System.out.println(errorStandardDTO.toString());
//        managementService.updateErrorStandard(errorStandardDTO, processcode);
//        return criteriaService.findErrorCriteriaByProcessCode(processcode);
//    }
}