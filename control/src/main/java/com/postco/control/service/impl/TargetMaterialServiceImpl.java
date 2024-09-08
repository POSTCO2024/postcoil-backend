package com.postco.control.service.impl;

import com.postco.control.domain.repository.ErrorCriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TargetMaterialServiceImpl  {
    private final ErrorCriteriaRepository errorCriteriaRepository;
}

