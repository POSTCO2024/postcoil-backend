package com.postco.control.service;

import com.postco.control.domain.ErrorCriteria;
import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.repository.ErrorCriteriaDetailRepository;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.presentation.dto.response.ErrorStandardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ErrorManagementService {
    private final ErrorCriteriaDetailRepository errorCriteriaDetailRepository;
    private final ErrorCriteriaRepository errorCriteriaRepository;

    @Transactional
    public void updateErrorStandard(ErrorStandardDTO errorStandardDTO, String processcode) {
        ErrorCriteriaMapper mapper = errorCriteriaRepository.findByProcessCode(processcode)
                .orElseThrow(() -> new IllegalStateException("no such code"));

        List<ErrorCriteria> errorCriteriaList = mapper.getErrorCriteria();
        for (ErrorCriteria errorCriteria : errorCriteriaList) {
            String columnName = errorCriteria.getColumnName();
            switch (columnName) {
                case "min_thickness":
                    if (errorStandardDTO.getMinThickness() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getMinThickness());
                    }
                    break;
                case "max_thickness":
                    if (errorStandardDTO.getMaxThickness() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getMaxThickness());
                    }
                    break;
                case "min_width":
                    if (errorStandardDTO.getMinWidth() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getMinWidth());
                    }
                    break;
                case "max_width":
                    if (errorStandardDTO.getMaxWidth() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getMaxWidth());
                    }
                    break;
                case "coil_type_code":
                    if (errorStandardDTO.getCoilTypeCode() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getCoilTypeCode());
                    }
                    break;
                case "factory_code":
                    if (errorStandardDTO.getFactoryCode() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getFactoryCode());
                    }
                    break;
                case "order_no":
                    if (errorStandardDTO.getOrderNo() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getOrderNo());
                    }
                    break;
                case "rem_proc":
                    if (errorStandardDTO.getRemProc() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getRemProc());
                    }
                    break;
                case "roll_unit":
                    if (errorStandardDTO.getRollUnit() != null) {
                        errorCriteria.setColumnValue(errorStandardDTO.getRollUnit());
                    }
                    break;
            }
            errorCriteriaDetailRepository.save(errorCriteria);
        }
    }
}
