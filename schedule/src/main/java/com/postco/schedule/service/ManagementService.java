package com.postco.schedule.service;

import com.postco.schedule.domain.repository.ConstraintInsertionRepository;
import com.postco.schedule.domain.repository.PriorityRepository;
import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;
import com.postco.schedule.presentation.dto.ManagementDTO;
import com.postco.schedule.presentation.dto.PriorityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagementService {

    private final PriorityRepository priorityRepository;
    private final ConstraintInsertionRepository constraintInsertionRepository;

    public ManagementDTO findManagementDataByProcessCodeAndMaterialUnitCode(String processCode, String materialUnitCode) {

        List<PriorityDTO> filteredPriority = priorityRepository.findByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode).stream()
                .map(priority -> PriorityDTO.builder()
                        .id(priority.getId())
                        .name(priority.getName())
                        .priorityOrder(priority.getPriorityOrder())
                        .applyMethod(String.valueOf(priority.getApplyMethod()))
                        .targetColumn(priority.getTargetColumn())
                        .build())
                .collect(Collectors.toList());

        List<ConstraintInsertionDTO> filteredConstraintInsertion = constraintInsertionRepository.findByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode).stream()
                .map(constraintInsertion -> ConstraintInsertionDTO.builder()
                        .id(constraintInsertion.getId())
                        .type(String.valueOf(constraintInsertion.getType()))
                        .targetColumn(constraintInsertion.getTargetColumn())
                        .targetValue(String.valueOf(constraintInsertion.getTargetValue()))
                        .build())
                .collect(Collectors.toList());

        return ManagementDTO.builder()
                .priorities(filteredPriority)
                .constraintInsertions(filteredConstraintInsertion)
                .build();
    }
}
