package com.postco.schedule.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.repository.ConstraintInsertionRepository;
import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConstraintInsertionService {
    private final ConstraintInsertionRepository constraintInsertionRepository;

    public List<ConstraintInsertionDTO> findAllByProcessCodeAndMaterialUnitCode(String processCode, String materialUnitCode) {
    return MapperUtils.mapList(constraintInsertionRepository.findByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode), ConstraintInsertionDTO.class);
    }
}
