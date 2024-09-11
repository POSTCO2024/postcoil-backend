package com.postco.control.service;

import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.repository.ErrorCriteriaDetailRepository;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.presentation.dto.response.ErrorStandardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagementService {
    private final ErrorCriteriaDetailRepository errorCriteriaDetailRepository;
    private final ErrorCriteriaRepository errorCriteriaRepository;

    @Transactional
    public void updateErrorStandard(ErrorStandardDTO errorStandardDTO, String processcode) {
        ErrorCriteriaMapper mapper = errorCriteriaRepository.findByProcessCode(processcode)
                .orElseThrow(() -> new IllegalStateException("no such code"));

        mapper.updateErrorCriteria(errorStandardDTO);
        errorCriteriaRepository.save(mapper);

    }
}
