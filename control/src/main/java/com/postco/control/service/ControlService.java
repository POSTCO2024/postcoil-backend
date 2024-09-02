package com.postco.control.service;

import com.postco.control.domain.*;
import com.postco.control.domain.repository.ExtractionCriteriaRepository;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.domain.repository.MaterialsRepository;
import com.postco.control.domain.repository.OrderRepository;
import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.control.service.impl.TargetMaterialServiceImpl;
import com.postco.core.utils.mapper.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ControlService  implements TargetMaterialService{

    private final ExtractionCriteriaRepository extractionCriteriaRepository;
    private final ErrorCriteriaRepository errorCriteriaRepository;
    private final MaterialsRepository materialsRepository;

    @Autowired
    public ControlService(ExtractionCriteriaRepository extractionCriteriaRepository,
                          ErrorCriteriaRepository errorCriteriaRepository,
                          MaterialsRepository materialsRepository,
                          OrderRepository ordersRepository) {
        this.extractionCriteriaRepository = extractionCriteriaRepository;
        this.errorCriteriaRepository = errorCriteriaRepository;
        this.materialsRepository = materialsRepository;
    }

    // 재료(Materials) DTO 형태로 가져오기
    public List<MaterialDTO> findMaterial() {
        List<MaterialDTO> material = MapperUtils.mapList(materialsRepository.findAll(), MaterialDTO.class);
        return material;
    }

    /**
     * ExtractionCriteria 테이블의 모든 기준을 가져와 Materials 테이블에서 조건에 맞는 주문을 추출합
     *
     * @return 조건에 맞는 Materials 리스트
     */
    public List<MaterialDTO> getFilteredMaterials() {
        List<MaterialDTO> materials = findMaterial();   // 임의 데이터 호출
        System.out.println("==== Dataset(input): " + materials);


        //  추출 기준 & 에러 기준
        Optional<ExtractionCriteriaMapper> extraction = extractionCriteriaRepository.findByProcessCode("1PCM");  // 1PCM으로 고정
        Optional<ErrorCriteriaMapper> error = errorCriteriaRepository.findByProcessCode("1PCM");
        System.out.println("==== Extraction: "+ extraction);
        System.out.println("==== Error: "+ error);


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

        System.out.println("전체 재료 목록이 반환되었습니다.");
        return findMaterial();
    }

    // Filtering
    private List<MaterialDTO> filterMaterials(List<MaterialDTO> materials, String fCode, String status, String processCode, String currProcessCode) {
        return materials.stream()
                .filter(material -> (fCode == null || material.getFCode().equals(fCode)))
                .filter(material -> (status == null || String.valueOf(material.getStatus()).equals(status)))
                .filter(material -> (processCode == null || material.getProgress().equals(processCode)))
                .filter(material -> (currProcessCode == null || material.getCurrProc().equals(currProcessCode)))
                .collect(Collectors.toList());
    }

}
