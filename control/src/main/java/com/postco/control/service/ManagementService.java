package com.postco.control.service;

import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.ExtractionCriteria;
import com.postco.control.domain.ExtractionCriteriaMapper;
import com.postco.control.domain.repository.ErrorCriteriaDetailRepository;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.domain.repository.ExtractionCriteriaDetailRepository;
import com.postco.control.domain.repository.ExtractionCriteriaRepository;
import com.postco.control.presentation.dto.response.ErrorStandardDTO;
import com.postco.control.presentation.dto.response.ExtractionStandardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagementService {
    private final ErrorCriteriaDetailRepository errorCriteriaDetailRepository;
    private final ErrorCriteriaRepository errorCriteriaRepository;
    private final ExtractionCriteriaRepository extractionCriteriaRepository;
    private final ExtractionCriteriaDetailRepository extractionCriteriaDetailRepository;

    @Transactional
    public void updateErrorStandard(ErrorStandardDTO errorStandardDTO, String processcode) {
        ErrorCriteriaMapper mapper = errorCriteriaRepository.findByProcessCode(processcode)
                .orElseThrow(() -> new IllegalStateException("no such code"));

        mapper.updateErrorCriteria(errorStandardDTO);
        errorCriteriaRepository.save(mapper);

    }

    @Transactional
    public void updateExtractStandard(ExtractionStandardDTO extractionStandardDTO, String processCode) {
        ExtractionCriteriaMapper mapper = extractionCriteriaRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new IllegalStateException("no such code"));

        List<ExtractionCriteria> extractionCriteriaList = mapper.getExtractionCriteria();
        System.out.println(extractionStandardDTO);
        for (ExtractionCriteria criteria : extractionCriteriaList) {
            String columnName = criteria.getColumnName();
            switch (columnName) {
                case "factory_code":
                    criteria.setColumnValue(extractionStandardDTO.getFactoryCode());
                    break;
                case "material_status":
                    criteria.setColumnValue(extractionStandardDTO.getMaterialStatus());
                    break;
                case "progress":
                    criteria.setColumnValue(extractionStandardDTO.getProgress());
                    break;
                case "curr_proc":
                    criteria.setColumnValue(extractionStandardDTO.getCurrProc());
                    break;
            }
            extractionCriteriaDetailRepository.save(criteria);
        }
    }
}
