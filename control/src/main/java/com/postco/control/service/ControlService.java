package com.postco.control.service;

import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.ExtractionCriteria;
import com.postco.control.domain.ExtractionCriteriaMapper;
import com.postco.control.domain.Order;
import com.postco.control.domain.repository.ExtractionCriteriaRepository;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class ControlService {

    private final ExtractionCriteriaRepository extractionCriteriaRepository;
    private final ErrorCriteriaRepository errorCriteriaRepository;
    private final OrderRepository ordersRepository;

    @Autowired
    public ControlService(ExtractionCriteriaRepository extractionCriteriaRepository,
                          ErrorCriteriaRepository errorCriteriaRepository,
                          OrderRepository ordersRepository) {
        this.extractionCriteriaRepository = extractionCriteriaRepository;
        this.errorCriteriaRepository = errorCriteriaRepository;
        this.ordersRepository = ordersRepository;
    }

    /**
     * ExtractionCriteria 테이블의 모든 기준을 가져와 Orders 테이블에서 조건에 맞는 주문을 추출합
     *
     * @return 조건에 맞는 Orders 리스트
     */
    public List<Order> getFilteredOrders() {
        //  추출 기준 & 에러 기준
        Optional<ExtractionCriteriaMapper> extraction = extractionCriteriaRepository.findByProcessCode("1PCM");  // 1PCM으로 고정
        Optional<ErrorCriteriaMapper> error = errorCriteriaRepository.findByProcessCode("1PCM");
        System.out.println("==== Extraction: "+ extraction);
        System.out.println("==== Error: "+ error);


        extraction.ifPresent(criteriaMapper -> {
            // processCode 값 사용
            String processCode = criteriaMapper.getProcessCode();
            System.out.println("Process Code: " + processCode);

            // extractionGroup 값 사용
            String extractionGroup = criteriaMapper.getExtractionGroup();
            System.out.println("Extraction Group: " + extractionGroup);

            // extractionCriteria 리스트 사용
            List<ExtractionCriteria> criteriaList = criteriaMapper.getExtractionCriteria();

            // 리스트 내 각 항목 접근
            for (ExtractionCriteria criteria : criteriaList) {
                String columnName = criteria.getColumnName();
                String columnValue = criteria.getColumnValue();
                System.out.println("Column Name: " + columnName + ", Column Value: " + columnValue);
            }
        });

        // 예시: 모든 기준을 AND 조건으로 적용하여 Orders를 필터링
        // 실제로는 criteriaList를 기반으로 동적 쿼리를 작성해야 합니다.
        // 여기서는 단순히 예시로 모든 Orders를 반환합니다.
        // 동적 쿼리를 구현하려면 Specification이나 QueryDSL을 사용하는 것이 좋습니다.



        // with 조건에 맞는 값 추출
        return ordersRepository.findByWidthGreaterThanEqualAndWidthLessThanEqual(820.0, 1105.0);
    }

}
