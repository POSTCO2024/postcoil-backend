package com.postco.control.service;

import com.postco.control.domain.*;
import com.postco.control.domain.repository.*;
import com.postco.control.presentation.dto.response.Fc002DTO;
import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.control.presentation.dto.response.TargetMaterialDTO;
import com.postco.core.utils.mapper.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                          TargetMaterialRepository targetMaterialRepository
    ) {
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
//        for (JoinTables joinTables : joinTablesRepository.findAll()) {
//            System.out.println("\n"+joinTables.toString()+"\n");
//        }
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
    public List<MaterialDTO> getFilteredExtractionMaterials() {
        List<MaterialDTO> materials = findJoinTables();   // 임의 데이터 호출
        System.out.println("==== Dataset(input): " + materials);


        //  추출 기준
        Optional<ExtractionCriteriaMapper> extraction = extractionCriteriaRepository.findByProcessCode("1PCM");  // 1PCM으로 고정 => 고정하지 않을 경우, input으로 받아야함
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
            System.out.println("필터링이 완료되었습니다.");
            System.out.println("Filtered Materials: " + filteredMaterials);

            return filteredMaterials;
        }

        System.out.println("추출 기준이 존재하지 않습니다.");
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
    public List<TargetMaterialDTO.Create> extractMaterialByErrorCriteria(List<MaterialDTO> materials) {
        ErrorCriteriaMapper mapper = errorCriteriaRepository.findByProcessCode("1PCM")   // 임의로 설정 - MaterialDTO와 함께 input으로 받을 것
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
        System.out.println("\n\n\n" + materialsList + "\n\n\n");
        List<TargetMaterialDTO.Create> targetMaterialList = MapperUtils.mapList(materialsList, TargetMaterialDTO.Create.class);
        return targetMaterialList;
    }


    /**
     * step 3) by.leeyc
     * 추출 & 에러 기준에 만족한 TargetMaterial 리스트에서 롤 단위를 추가함
     *
     * @return 작업 대상재 테이블의 입력값인 TargetMaterials 리스트
     */
    // 롤 단위(A/B) 매핑
    public List<TargetMaterialDTO.Create> createRollUnit(List<TargetMaterialDTO.Create> materials) {
        for (TargetMaterialDTO.Create material : materials) {
            System.out.println("[debug] 주문 두께: " + material.getGoalWidth());
            if (material.getGoalWidth() < 600) {
                material.setRollUnit("A");  // A단위(박물)
            } else {
                material.setRollUnit("B");  // B단위(후물)
            }
        }


        /** step4) 작업 대상재 DB에 insert
         * 작업 대상재 DB를 초기화하고, 현재 재료 기준으로 작업 대상재를 추출함
         *
         * */
        List<TargetMaterial> targetMaterials = MapperUtils.mapList(materials, TargetMaterial.class);
        System.out.println("\n\n\n" + targetMaterials + "\n\n\n");
        targetMaterialRepository.deleteAll();  // 테이블 초기화
        targetMaterialRepository.saveAll(targetMaterials);

        return materials;
    }

    public List<Fc002DTO> findErrorMaterial() {
        List<TargetMaterial> errorMaterials = targetMaterialRepository.findByIsErrorIs("Y");
        return MapperUtils.mapList(errorMaterials, Fc002DTO.class);
    }

}
