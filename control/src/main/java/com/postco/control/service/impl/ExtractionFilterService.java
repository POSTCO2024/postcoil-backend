package com.postco.control.service.impl;

import com.postco.control.domain.ExtractionCriteria;
import com.postco.control.domain.ExtractionCriteriaMapper;
import com.postco.control.domain.repository.ExtractionCriteriaRepository;
import com.postco.control.service.ExtractionFilter;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionFilterService {
    private final ExtractionCriteriaRepository extractionCriteriaRepository;

    public List<MaterialDTO.View> applyExtractionCriteria(List<MaterialDTO.View> materials, String processCode) {
        ExtractionCriteriaMapper mapper = extractionCriteriaRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new IllegalArgumentException("No extraction criteria found for process code: " + processCode));
        List<ExtractionCriteria> criteria = mapper.getExtractionCriteria();

        return materials.stream()
                .filter(material -> criteria.stream().allMatch(criterion -> applyFilter(material, criterion)))
                .collect(Collectors.toList());
    }

    private boolean applyFilter(MaterialDTO.View material, ExtractionCriteria criterion) {
        ExtractionFilter filter = ExtractionFilter.fromColumnName(criterion.getColumnName());
        String value = criterion.getColumnValue();

        switch (filter) {
            case FACTORY_CODE:
                return value.equals(material.getFactoryCode());
            case MATERIAL_STATUS:
                return value.equals(String.valueOf(material.getStatus()));
            case PROGRESS:
                return value.equals(material.getProgress());
            case CURRENT_PROCESS:
                return material.getCurrProc() != null && material.getCurrProc().equalsIgnoreCase(value);
            default:
                log.warn("Unknown filter type: {}", filter);
                return false;
        }
    }
}