package com.postco.control.service;

import com.postco.control.domain.*;
import com.postco.control.domain.repository.ExtractionCriteriaRepository;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.domain.repository.MaterialsRepository;
import com.postco.control.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class ControlService {

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

    /**
     * ExtractionCriteria 테이블의 모든 기준을 가져와 Materials 테이블에서 조건에 맞는 주문을 추출합
     *
     * @return 조건에 맞는 Materials 리스트
     */
    public List<Materials> getFilteredMaterials() {
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

            // 모든 조건이 설정된 경우
            if(fCode!=null && status!=null && processCode!=null && currProcessCode!=null) {
                System.out.println("정상 추출되었습니다.");
                return materialsRepository.findAllByfCodeAndStatusAndProgressAndCurrProc(fCode, status, processCode, currProcessCode);
            } else {
                System.out.println("추출 조건이 존재하지 않습니다.");
                System.out.println("조건: " + "fCode: " + fCode + ", status: " + status + ", processCode: " + processCode + ", currProcessCode: " + currProcessCode);
                return List.of();  // 빈 리스트 반환
            }

        }

        System.out.println("전체 재료 목록이 반환되었습니다.");
        return materialsRepository.findAll();
    }

}
