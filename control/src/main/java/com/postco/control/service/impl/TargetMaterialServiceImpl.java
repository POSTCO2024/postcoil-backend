package com.postco.control.service.impl;

import com.postco.control.domain.ErrorCriteria;
import com.postco.control.domain.ErrorCriteriaMapper;
import com.postco.control.domain.repository.ErrorCriteriaRepository;
import com.postco.control.domain.repository.MaterialsRepository;
import com.postco.control.presentation.dto.response.MaterialDTO;
import com.postco.control.service.TargetMaterialService;
import com.postco.core.utils.mapper.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TargetMaterialServiceImpl  {
    private final ErrorCriteriaRepository errorCriteriaRepository;
    private final MaterialsRepository materialsRepository;


}

