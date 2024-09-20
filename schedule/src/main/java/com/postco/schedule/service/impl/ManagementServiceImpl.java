package com.postco.schedule.service.impl;

import com.postco.schedule.presentation.dto.ConstraintInsertionDTO;
import com.postco.schedule.presentation.dto.ManagementDTO;
import com.postco.schedule.presentation.dto.PriorityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagementServiceImpl {

    private final PriorityServiceImpl priorityService;
    private final ConstraintInsertionServiceImpl constraintInsertionService;

    public ManagementDTO findManagementDataByProcessCodeAndRollUnit(String processCode, String rollUnit) {

        List<PriorityDTO> filteredPriority = priorityService.findAllByProcessCodeAndRollUnit(processCode, rollUnit);
        List<ConstraintInsertionDTO> filteredConstraintInsertion = constraintInsertionService.findAllByProcessCodeAndRollUnit(processCode, rollUnit);

        return ManagementDTO.builder()
                .priorities(filteredPriority)
                .constraintInsertions(filteredConstraintInsertion)
                .build();
    }
}
