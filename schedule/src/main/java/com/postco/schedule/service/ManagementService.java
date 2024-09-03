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

    private final PriorityService priorityService;
    private final ConstraintInsertionService constraintInsertionService;

    public ManagementDTO findManagementDataByProcessCodeAndMaterialUnitCode(String processCode, String materialUnitCode) {

        List<PriorityDTO> filteredPriority = priorityService.findAllByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode);
        List<ConstraintInsertionDTO> filteredConstraintInsertion = constraintInsertionService.findAllByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode);

        return ManagementDTO.builder()
                .priorities(filteredPriority)
                .constraintInsertions(filteredConstraintInsertion)
                .build();
    }
}
