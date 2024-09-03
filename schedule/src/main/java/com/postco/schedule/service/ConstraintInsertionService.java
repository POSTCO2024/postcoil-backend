package com.postco.schedule.service;

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
    return constraintInsertionRepository.findByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode).stream()
            .map(constraintInsertion -> ConstraintInsertionDTO.builder()
                    .id(constraintInsertion.getId())
                    .type(String.valueOf(constraintInsertion.getType()))
                    .targetColumn(constraintInsertion.getTargetColumn())
                    .targetValue(String.valueOf(constraintInsertion.getTargetValue()))
                    .build())
            .collect(Collectors.toList());

    }
}
