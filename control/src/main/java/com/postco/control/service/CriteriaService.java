package com.postco.control.service;

import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.ExtractionCriteriaMapper;
import com.postco.control.presentation.dto.response.CriteriaDTO;

import java.util.List;

public interface CriteriaService {
    CriteriaDTO findErrorCriteriaByProcessCode(String processCode);
    CriteriaDTO findExtractionCriteriaByProcessCode(String processCode);
}
