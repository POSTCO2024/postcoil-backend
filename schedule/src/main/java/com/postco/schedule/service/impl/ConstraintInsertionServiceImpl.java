package com.postco.schedule.service.impl;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.repository.ConstraintInsertionRepository;
import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;
import com.postco.schedule.service.ConstraintInsertionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConstraintInsertionServiceImpl implements ConstraintInsertionService {
    private final ConstraintInsertionRepository constraintInsertionRepository;

    public List<ConstraintInsertionDTO> findAllByProcessCodeAndRollUnit(String processCode, String rollUnit) {
        return MapperUtils.mapList(constraintInsertionRepository.findByProcessCodeAndRollUnit(processCode, rollUnit), ConstraintInsertionDTO.class);
    }

    public List<ConstraintInsertionDTO> findByProcessCode(String processCode){
        return MapperUtils.mapList(constraintInsertionRepository.findByProcessCode(processCode), ConstraintInsertionDTO.class);
    }
}
