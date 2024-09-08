package com.postco.control.service;

import com.postco.control.domain.*;
import com.postco.control.domain.repository.*;
import com.postco.control.presentation.ControlController;
import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.control.presentation.dto.response.Fc002DTO;
import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.control.presentation.dto.response.TargetMaterialDTO;
import com.postco.core.utils.mapper.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ControlService implements TargetMaterialService {

    private final ExtractionCriteriaRepository extractionCriteriaRepository;
    private final ErrorCriteriaRepository errorCriteriaRepository;
    private final MaterialsRepository materialsRepository;
    private final JoinTablesRepository joinTablesRepository;
    private final TargetMaterialRepository targetMaterialRepository;

    @Autowired
    public ControlService(ExtractionCriteriaRepository extractionCriteriaRepository,
                          ErrorCriteriaRepository errorCriteriaRepository,
                          MaterialsRepository materialsRepository,
                          JoinTablesRepository joinTablesRepository,
                          TargetMaterialRepository targetMaterialRepository) {
        this.extractionCriteriaRepository = extractionCriteriaRepository;
        this.errorCriteriaRepository = errorCriteriaRepository;
        this.materialsRepository = materialsRepository;
        this.joinTablesRepository = joinTablesRepository;
        this.targetMaterialRepository = targetMaterialRepository;
    }

    // 재료(Materials) DTO 형태로 가져오기
    public List<MaterialDTO> findMaterial() {
        List<MaterialDTO> materials = MapperUtils.mapList(materialsRepository.findAll(), MaterialDTO.class);
        return materials;
    }


    public List<MaterialDTO> findJoinTables() {
        List<JoinTables> joinTablesList = joinTablesRepository.findAll();
        joinTablesList.forEach(joinTable -> System.out.println("JoinTable ID: " + joinTable.getMaterialId()));
        return MapperUtils.mapList(joinTablesRepository.findAll(), MaterialDTO.class);
    }

    /**
     * step 1) by.leeyc
     * ExtractionCriteria 테이블의 모든 기준을 가져와 Materials 테이블에서 조건에 맞는 주문을 추출합
     *
     * @return 추출 조건에 맞는 Materials 리스트
     */
    public List<MaterialDTO> getFilteredExtractionMaterials(String curProcCode) {
        List<MaterialDTO> materials = findJoinTables();   // 임의 데이터 호출
        System.out.println("==== Dataset(input): " + materials);


        //  추출 기준
        Optional<ExtractionCriteriaMapper> extraction;  // for Test

        if(curProcCode.isEmpty()){
            extraction = extractionCriteriaRepository.findByProcessCode("1PCM");
            System.out.println("[info] 공정이 선택되지 않았습니다. ");
        } else {
            extraction = extractionCriteriaRepository.findByProcessCode(curProcCode);
            System.out.println("[info] " + curProcCode + " 공정이 선택되었습니다. ");
        }

        System.out.println("==== Extraction: " + extraction);


        if (extraction.isPresent()) {
            ExtractionCriteriaMapper extractionCriteriaMapper = extraction.get();

            // 초기화
            String fCode = null;
            String status = null;
            String processCode = null;
            String currProcessCode = null;

            List<ExtractionCriteria> criteriaList = extractionCriteriaMapper.getExtractionCriteria();

            for (ExtractionCriteria criteria : criteriaList) {
                String columnsName = criteria.getColumnName();
                String columnsValue = criteria.getColumnValue();

                // debug
                System.out.println("Column Name: " + columnsName + ", Column Value: " + columnsValue);

                switch (columnsName) {
                    case "factory_code":
                        fCode = columnsValue;
                        break;
                    case "material_status":
                        status = columnsValue;
                        break;
                    case "progress":
                        processCode = columnsValue;
                        break;
                    case "curr_proc":
                        currProcessCode = columnsValue;
                        break;
                    default:
                        break;
                }
            }

            List<MaterialDTO> filteredMaterials = filterMaterials(materials, fCode, status, processCode, currProcessCode);
            System.out.println("[info] 추출 기준 필터링이 완료되었습니다.");
            System.out.println("Filtered Materials: " + filteredMaterials);

            return filteredMaterials;
        }

        System.out.println("[info] 추출 기준이 존재하지 않습니다.");
        return findMaterial();  // 전체 목록 반환
    }


    // 추출(Extraction) 기준 필터링
    private List<MaterialDTO> filterMaterials(List<MaterialDTO> materials, String fCode, String status, String processCode, String curProcCode) {
        return materials.stream()
                .filter(material -> (fCode == null || material.getFCode().equals(fCode)))
                .filter(material -> (status == null || String.valueOf(material.getStatus()).equals(status)))
                .filter(material -> (processCode == null || material.getProgress().equals(processCode)))
                .filter(material -> (curProcCode == null || material.getCurProcCode().equals(curProcCode)))
                .collect(Collectors.toList());
    }


    /**
     * step 2) by.pinky
     * 1. ErrorCriteria 테이블을 기준으로 에러 여부에 따라 error_flag를 추가
     * 2. MaterialDTO -> TargetMaterialDTO
     *
     * @return 에러 여부를 포함한 TargetMaterials 리스트
     */
    public List<TargetMaterialDTO.Create> extractMaterialByErrorCriteria(List<MaterialDTO> materials, String procCode) {
        ErrorCriteriaMapper mapper = errorCriteriaRepository.findByProcessCode(procCode)   // 임의로 설정 - MaterialDTO와 함께 input으로 받을 것
                .orElseThrow(() -> new IllegalArgumentException("no such code"));
        List<ErrorCriteria> criteria = mapper.getErrorCriteria();

        // 정렬해서 에러재 타입이 1. 정보이상재 , 2. 설비이상에러재, 3. 관리재 순으로 입력되게 함
        criteria.sort(Comparator.comparing(ErrorCriteria::getErrorType).reversed());

        List<MaterialDTO> materialsList = materials.stream()
                .peek(material -> {
                    List<String> errorType = new ArrayList<>();
                    boolean matchesCriteria = criteria.stream().allMatch(criterion -> {
                        String columnName = criterion.getColumnName();
                        String columnValue = criterion.getColumnValue();
                        String currentErrorType = criterion.getErrorType();

                        switch (columnName) {
                            case "min_thickness":
                                if (material.getThickness() < Double.parseDouble(columnValue)) {
                                    errorType.add(currentErrorType);
                                    return false;
                                }
                                return true;
                            case "max_thickness":
                                if (material.getThickness() > Double.parseDouble(columnValue)) {
                                    errorType.add(currentErrorType);
                                    return false;
                                }
                                return true;
                            case "max_width":
                                if (material.getWidth() > Double.parseDouble(columnValue)) {
                                    errorType.add(currentErrorType);
                                    return false;
                                }
                                return true;
                            case "min_width":
                                if (material.getWidth() < Double.parseDouble(columnValue)) {
                                    errorType.add(currentErrorType);
                                    return false;
                                }
                                return true;
                            case "coil_type_code":
                                if (material.getCoilTypeCode() == null) {
                                    errorType.add("정보이상재");
                                    return false;
                                } else if ((!currentErrorType.equals("정보이상재")) && (material.getCoilTypeCode().equals(columnValue))) {
                                    errorType.add("관리재");
                                    return false;
                                }
                                return true;
                            case "factory_code":
                                if (material.getFCode() == null) {
                                    errorType.add(currentErrorType);
                                    return false;
                                }
                                return true;
                            case "order_no":
                                if (material.getOrderNo() == null) {
                                    errorType.add(currentErrorType);
                                    return false;
                                }
                                return true;
                            case "rem_proc":
                                if (material.getRemProc() == null) {
                                    errorType.add(currentErrorType);
                                    return false;
                                }
                                return true;
                            default:
                                return true;
                        }
                    });
                    if (matchesCriteria) {
                        material.setIsError("N");
                    } else {
                        material.setIsError("Y");
                        material.setErrorType(errorType.get(errorType.size() - 1));
                    }

                }).collect(Collectors.toList());
        List<TargetMaterialDTO.Create> targetMaterialList = MapperUtils.mapList(materialsList, TargetMaterialDTO.Create.class);
        System.out.println("[info] 에러 기준 필터링이 완료되었습니다. ");
        return targetMaterialList;
    }


    /**
     * step 3) by.leeyc
     * 1. 추출 & 에러 기준에 만족한 TargetMaterial 리스트에서 롤 단위를 추가함
     * 2. 필터링 기준(공정)을 추가함
     *
     * @return 작업 대상재 테이블의 입력값인 TargetMaterials 리스트
     */
    // 롤 단위(A/B) 매핑
    public List<TargetMaterialDTO.Create> createRollUnit(List<TargetMaterialDTO.Create> materials, String procCode) {
        for (TargetMaterialDTO.Create material : materials) {
            System.out.println("[debug] 주문 두께: " + material.getGoalWidth());
            if (material.getGoalWidth() < 600) {
                material.setRollUnitName("A");  // A단위(박물)
            } else {
                material.setRollUnitName("B");  // B단위(후물)
            }

            // 필터링 기준 컬럼 추가
            material.setCriteria(procCode);
        }


        /** step4) 작업 대상재 DB에 insert
         * 작업 대상재 DB를 초기화하고, 현재 재료 기준으로 작업 대상재를 추출함
         *
         * */
        List<TargetMaterial> targetMaterials = MapperUtils.mapList(materials, TargetMaterial.class);
        // targetMaterialRepository.deleteAll();  // 테이블 초기화
        // To do: 작업대상대 ID 부여하기


        System.out.println("[info] 작업대상재: " + targetMaterials.toString());
        targetMaterialRepository.saveAll(targetMaterials);

        return materials;
    }

    /**
     * Repo 에서 에러재 추출
     *
     * @return 에러재
     */
//    public List<Fc002DTO> getErrorMaterials(String curProcCode) {
//        System.out.println("[info] " + curProcCode + " 공정을 조회합니다. ");
//        List<TargetMaterial> errorMaterials = targetMaterialRepository.findByIsErrorAndCriteria("Y", curProcCode);
//        return MapperUtils.mapList(errorMaterials, Fc002DTO.class);
//    }


    /**
     * fc001a: 작업대상재 관리 화면
     *
     * @return 작업 대상재 목록
     */
    public List<Fc001aDTO> getNormalMaterials(String curProcCode) {
        System.out.println("[info] " + curProcCode + " 공정을 조회합니다. ");
        List<TargetMaterial> targetMaterials = targetMaterialRepository.findByIsErrorAndCriteria("N", curProcCode);  // To do: 작업 대상재 추출 시 공정 기준을 추가
        return MapperUtils.mapList(targetMaterials, Fc001aDTO.class);
    }

    public List<TargetMaterialDTO.Table> getMaterialTable() {
        // 임의 데이터
        List<TargetMaterialDTO.View> materials = Arrays.asList(
                new TargetMaterialDTO.View(3, "CM240196", 2, null, "C", "1EGL", "C", "D", 449.0, 27.0, 690.0, 0.31, 508951.76, 0.001679, 854.53, "1PCM1CAL", "1EGL101", "1CAL", "101", "731732", "1EGLA", "HPKL", null),
                new TargetMaterialDTO.View(6, "CI958029", 2, null, "C", "EGL", "C", "D", 479.0, 24.0, 1521.0, 0.95, 189208.32, 0.011343, 2146.19, null, "2PCM2CAL1CGL201", null, "2EGL", "711512", "EGLA", "HTS500", null),
                new TargetMaterialDTO.View(7, "CO755025", 2, null, "C", "1CAL", "C", "D", 246.0, 28.0, 540.0, 0.19, 247043.48, 8.05E-4, 198.87, "1PCM", "1CAL1EGL101", "1PCM", "1EGL", "610233", "1CALA", "HPKL", null),
                new TargetMaterialDTO.View(2, "HJ762097", 2, null, "C", "2PCM", "H", "D", 581.0, 28.0, 975.0, 4.73, 55920.94, 0.036202, 2024.45, null, "2PCM1CAL1CGL201", null, "1CAL", "231112", "2PCMA", "HTS400", null),
                new TargetMaterialDTO.View(11, "HP555021", 2, null, "C", "1PCM", "H", "D", 548.0, 22.0, 1284.0, 3.51, 67087.26, 0.035379, 2373.48, null, "1PCM2CAL2EGL201", null, "2CAL", "211721", "1PCMA", "HCKP", null),
                new TargetMaterialDTO.View(8, "CW031869", 2, null, "C", "2EGL", "C", "D", 472.0, 20.0, 933.0, 0.58, 301134.65, 0.004248, 1279.22, "1PCM2CAL", "2EGL201", "2CAL", "201", "411313", "2EGLA", "HCKP", null),
                new TargetMaterialDTO.View(5, "CR008811", 2, null, "C", "EGL", "C", "D", 518.0, 28.0, 799.0, 1.17, 179604.8, 0.007338, 1317.94, null, "1PCM2CAL101", null, "201", "310933", "EGLA", "HTS600", null),
                new TargetMaterialDTO.View(10, "HU338413", 2, null, "C", "2PCM", "H", "D", 747.0, 29.0, 681.0, 3.3, 132608.13, 0.017641, 2339.34, null, "2PCM1CAL101", null, "1CAL", "311433", "2PCMA", "HTS300", null),
                new TargetMaterialDTO.View(9, "CJ682375", 2, null, "C", "1CAL", "C", "D", 472.0, 29.0, 1210.0, 0.65, 268176.22, 0.006174, 1655.72, "1PCM", "1CAL1EGL101", "1PCM", "1EGL", "320132", "1CALA", "HPKL", null),
                new TargetMaterialDTO.View(12, "HS922154", 2, null, "C", "1PCM", "H", "D", 627.0, 21.0, 857.0, 3.7, 83354.49, 0.024892, 2074.86, null, "1PCM2CAL1EGL201", null, "2CAL", "611021", "1PCMA", "HTS800", null),
                new TargetMaterialDTO.View(4, "CE312072", 2, null, "C", "2CAL", "C", "D", 338.0, 28.0, 849.0, 1.38, 64575.41, 0.009197, 593.9, "1PCM", "2CAL1EGL201", "1PCM", "1EGL", "730913", "2CALA", "HTS800", null),
                new TargetMaterialDTO.View(1, "CM692259", 2, null, "C", "1CAL", "C", "D", 432.0, 29.0, 677.0, 1.6, 91197.22, 0.008503, 775.45, "1PCM", "1CAL1EGL101", "1PCM", "1EGL", "731333", "1CALA", "HPKL", null)
        );

        System.out.println("Dataset: " + materials);


        // 품종(coilTypeCode) 별 차공정(nextProc) 카운트하기
        Map<String, TargetMaterialDTO.Table> resultMap = new HashMap<>();

        for (TargetMaterialDTO.View view : materials) {
            String coilTypeCode = view.getCoilTypeCode();
            String nextProc = view.getNextProc();
            //System.out.println("[debug] " + coilTypeCode + " " + nextProc);

            // 초기화 | 가져오기
            TargetMaterialDTO.Table table = resultMap.getOrDefault(coilTypeCode, new TargetMaterialDTO.Table(coilTypeCode, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));

            // 각 공정에 맞게 Count
            if ("1CAL".equals(nextProc)) {
                table.setProc1CAL(table.getProc1CAL() + 1);
            } else if ("2CAL".equals(nextProc)) {
                table.setProc2CAL(table.getProc2CAL() + 1);
            } else if ("1EGL".equals(nextProc)) {
                table.setProc1EGL(table.getProc1EGL() + 1);
            } else if ("2EGL".equals(nextProc)) {
                table.setProc2EGL(table.getProc2EGL() + 1);
            } else if ("1CGL".equals(nextProc)) {
                table.setProc1CGL(table.getProc1CGL() + 1);
            } else if ("2CGL".equals(nextProc)) {
                table.setProc2CGL(table.getProc2CGL() + 1);
            } else if ("101".equals(nextProc)) {
                table.setProc1Packing(table.getProc1Packing() + 1);
            } else if ("201".equals(nextProc)) {
                table.setProc2Packing(table.getProc2Packing() + 1);
            }

            // 총 합계 (Total Cnt) 계산하기
            table.setTotalCnt(table.getTotalCnt() + 1);

            resultMap.put(coilTypeCode, table); // save Result HashMap
        }

        // System.out.println("result1 [Map]: " + resultMap);
        System.out.println("result2 [List]: " + new ArrayList<>(resultMap.values()));

        return new ArrayList<>(resultMap.values());
    }

    /**
     * 에러 패스
     * @param errorMaterialIds
     */
    public void errorPass(List<Long> errorMaterialIds) {
        targetMaterialRepository.updateisError(errorMaterialIds);
    }
}
