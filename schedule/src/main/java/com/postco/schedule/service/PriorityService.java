package com.postco.schedule.service;

import com.postco.schedule.domain.repository.PriorityRepository;
import com.postco.schedule.presentation.dto.PriorityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriorityService {

    private final PriorityRepository priorityRepository;

    public List<PriorityDTO> findAllByProcessCodeAndMaterialUnitCode(String processCode, String materialUnitCode){
        return priorityRepository.findByProcessCodeAndMaterialUnitCode(processCode, materialUnitCode).stream()
                .map(priority -> PriorityDTO.builder()
                        .id(priority.getId())
                        .name(priority.getName())
                        .priorityOrder(priority.getPriorityOrder())
                        .applyMethod(priority.getApplyMethod())
                        .targetColumn(priority.getTargetColumn())
                        .build())
                .collect(Collectors.toList());
    }

    public List<PriorityDTO> findAllByProcessCode(String processCode){
        return priorityRepository.findByProcessCode(processCode).stream()
                .map(priority -> PriorityDTO.builder()
                        .id(priority.getId())
                        .name(priority.getName())
                        .priorityOrder(priority.getPriorityOrder())
                        .applyMethod(priority.getApplyMethod())
                        .targetColumn(priority.getTargetColumn())
                        .build())
                .collect(Collectors.toList());
    }
}
