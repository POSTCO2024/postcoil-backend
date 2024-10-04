package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.WorkScheduleSummary;
import com.postco.operation.domain.repository.WorkScheduleSummaryRepository;
import com.postco.operation.presentation.dto.WorkScheduleSummaryDTO;
import com.postco.operation.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonitoringServiceImpl implements MonitoringService {
    @Autowired
    private WorkScheduleSummaryRepository workScheduleSummaryRepository;

    @Override
    public Mono<List<WorkScheduleSummaryDTO>> getWorkScheduleSummary() {
        return Mono.fromCallable(() -> workScheduleSummaryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    private WorkScheduleSummaryDTO convertToDTO(WorkScheduleSummary workScheduleSummary) {
        return WorkScheduleSummaryDTO.builder()
                .process(workScheduleSummary.getProcess())
                .totalWorkInstructions(workScheduleSummary.getTotalWorkInstructions())
                .totalGoalCoils(workScheduleSummary.getTotalGoalCoils())
                .totalCompleteCoils(workScheduleSummary.getTotalCompleteCoils())
                .totalScheduledCoils(workScheduleSummary.getTotalScheduledCoils())
                .build();
    }
}
