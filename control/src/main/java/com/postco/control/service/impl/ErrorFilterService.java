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
                    .filter(m -> m.getId().equals(targetMaterial.getMaterialId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Material not found: " + targetMaterial.getMaterialId()));

            Map<String, Object> errorResult = determineErrorStatus(material, criteria);

            updateTargetMaterial(targetMaterial, errorResult);
        });
    }

    private Map<String, Object> determineErrorStatus(MaterialDTO.View material, List<ErrorCriteria> criteria) {
        // 정보이상재 체크
        Map<String, Object> infoErrorResult = checkInfoError(material);
        if ("Y".equals(infoErrorResult.get("isError"))) {
            return infoErrorResult;
        }

        // 설비이상에러재와 관리재 체크
        return checkCriteriaErrors(material, criteria);
    }

    private Map<String, Object> checkInfoError(MaterialDTO.View material) {
        if (material.getCoilTypeCode() == null || material.getFactoryCode() == null ||
                material.getOrderId() == null || material.getRemProc() == null) {
            return createErrorResult("Y", ErrorType.정보이상재);
        }
        return createErrorResult("N", null);
    }

    private Map<String, Object> checkCriteriaErrors(MaterialDTO.View material, List<ErrorCriteria> criteria) {
        for (ErrorCriteria criterion : criteria) {
            if (applyFilter(material, criterion)) {
                ErrorType errorType = (criterion.getErrorType() == ErrorType.관리재) ?
                        ErrorType.관리재 : ErrorType.설비이상에러재;
                return createErrorResult("Y", errorType);
            }
        }
        return createErrorResult("N", null);
    }

    private Map<String, Object> createErrorResult(String isError, ErrorType errorType) {
        Map<String, Object> result = new HashMap<>();
        result.put("isError", isError);
        result.put("errorType", errorType);
        return result;
    }

    private void updateTargetMaterial(TargetMaterial targetMaterial, Map<String, Object> errorResult) {
        targetMaterial.setIsError((String) errorResult.get("isError"));
        targetMaterial.setErrorType((ErrorType) errorResult.get("errorType"));
        targetMaterialRepository.save(targetMaterial);
    }

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