package com.postco.control.service.impl;

import com.postco.control.domain.ErrorCriteria;
import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.ErrorType;
import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.service.ErrorFilter;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorFilterService {
    private final ErrorCriteriaRepository errorCriteriaRepository;
    private final TargetMaterialRepository targetMaterialRepository;

    private static final List<ErrorType> ERROR_PRIORITY = List.of(
            ErrorType.정보이상재, ErrorType.설비이상에러재, ErrorType.관리재
    );

    @Transactional
    public void applyErrorCriteria(List<TargetMaterial> targetMaterials, List<MaterialDTO.View> materials, String processCode) {
        ErrorCriteriaMapper mapper = errorCriteriaRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new IllegalArgumentException("No error criteria found for process code: " + processCode));
        List<ErrorCriteria> criteria = mapper.getErrorCriteria();

        targetMaterials.forEach(targetMaterial -> {
            MaterialDTO.View material = materials.stream()
                    .filter(m -> m.getMaterialId().equals(targetMaterial.getMaterialId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Material not found: " + targetMaterial.getMaterialId()));

            // 에러 상태를 map으로 처리
            Map<String, Object> errorResult = determineErrorStatus(material, criteria);

            updateTargetMaterial(targetMaterial, errorResult);
        });
    }

    private ErrorCriteria findHighestPriorityError(MaterialDTO.View material, List<ErrorCriteria> criteria) {
        return ERROR_PRIORITY.stream()
                .map(errorType -> criteria.stream()
                        .filter(c -> c.getErrorType() == errorType && applyFilter(material, c))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    // 에러 결과를 Map으로 반환
    private Map<String, Object> determineErrorStatus(MaterialDTO.View material, List<ErrorCriteria> criteria) {
        return Optional.ofNullable(findHighestPriorityError(material, criteria))
                .map(error -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("isError", "Y");
                    result.put("errorType", error.getErrorType());
                    return result;
                })
                .orElseGet(() -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("isError", "N");
                    result.put("errorType", null);
                    return result;
                });
    }

    // 업데이트 로직
    private void updateTargetMaterial(TargetMaterial targetMaterial, Map<String, Object> errorResult) {
        targetMaterial.setIsError((String) errorResult.get("isError"));
        targetMaterial.setErrorType((ErrorType) errorResult.get("errorType"));
        targetMaterialRepository.save(targetMaterial);
    }

    // 필터 적용 로직
    private boolean applyFilter(MaterialDTO.View material, ErrorCriteria criterion) {
        ErrorFilter errorFilter = ErrorFilter.fromColumnName(criterion.getColumnName());
        String value = criterion.getColumnValue();

        switch (errorFilter) {
            case MIN_THICKNESS:
                return material.getThickness() < Double.parseDouble(value);
            case MAX_THICKNESS:
                return material.getThickness() > Double.parseDouble(value);
            case MIN_WIDTH:
                return material.getWidth() < Double.parseDouble(value);
            case MAX_WIDTH:
                return material.getWidth() > Double.parseDouble(value);
            case COIL_TYPE_CODE:
                return material.getCoilTypeCode() == null || material.getCoilTypeCode().equals(value);
            case FACTORY_CODE:
                return material.getFactoryCode() == null;
            case ORDER_ID:
                return material.getOrderId() == null;
            case REM_PROC:
                return material.getRemProc() == null;
            default:
                log.warn("Unknown error filter type: {}", errorFilter);
                return false;
        }
    }
}