package com.postco.control.service.impl;

import com.postco.control.domain.ErrorCriteria;
import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.ExtractionCriteria;
import com.postco.control.domain.ExtractionCriteriaMapper;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.domain.repository.ExtractionCriteriaRepository;
import com.postco.control.presentation.dto.response.CriteriaDTO;
import com.postco.control.presentation.dto.response.CriteriaDetailDTO;
import com.postco.control.service.CriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CriteriaServiceImpl implements CriteriaService {
    private final ErrorCriteriaRepository errorCriteriaRepository;
    private final ExtractionCriteriaRepository extractionCriteriaRepository;
    @Override
    public CriteriaDTO findErrorCriteriaByProcessCode(String processCode) {
        ErrorCriteriaMapper mapper = errorCriteriaRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new IllegalArgumentException("no such code"));

        // 상세 정보 변환
        List<CriteriaDetailDTO> mapperDetail = mapper.getErrorCriteria().stream()
                .map(errorCriteria -> CriteriaDetailDTO.builder()
                        .columnName(errorCriteria.getColumnName())
                        .columnValue(errorCriteria.getColumnValue())
                        .build())
                .collect(Collectors.toList());

        return CriteriaDTO.builder()
                .processCode(processCode)
                .criteriaGroup(mapper.getErrorGroup())
                .criteriaDetails(mapperDetail)
                .build();
    }

    @Override
    public CriteriaDTO findExtractionCriteriaByProcessCode(String processCode) {
        ExtractionCriteriaMapper mapper = extractionCriteriaRepository.findByProcessCode(processCode)
                .orElseThrow(() -> new IllegalStateException("no such code"));

        List<CriteriaDetailDTO> mapperDetail = mapper.getExtractionCriteria().stream()
                .map(extractionCriteria -> CriteriaDetailDTO.builder()
                        .columnName(extractionCriteria.getColumnName())
                        .columnValue(extractionCriteria.getColumnValue())
                        .build())
                .collect(Collectors.toList());

        return CriteriaDTO.builder()
                .processCode(processCode)
                .criteriaGroup(mapper.getExtractionGroup())
                .criteriaDetails(mapperDetail)
                .build();
    }
}
