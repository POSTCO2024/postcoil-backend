package com.postco.operation.service;

import com.postco.operation.domain.entity.WorkScheduleSummary;
import com.postco.operation.domain.repository.WorkScheduleSummaryRepository;
import com.postco.operation.presentation.dto.WorkScheduleSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoritoringService {
    @Autowired
    private WorkScheduleSummaryRepository workScheduleSummaryRepository;

    /**
     * 일일 작업 진행 현황 요약 데이터를 비동기적으로 조회
     *
     * @return Mono<List<WorkScheduleSummaryDTO>> 일일 작업 진행 현황 요약 DTO 리스트
     */
    public Mono<List<WorkScheduleSummaryDTO>> getWorkScheduleSummary() {
        return Mono.fromCallable(() -> workScheduleSummaryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    /**
     * WorkScheduleSummary 엔티티를 WorkScheduleSummaryDTO로 변환하는 메서드
     *
     * @param workScheduleSummary 엔티티
     * @return WorkScheduleSummaryDTO 변환된 DTO
     */
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
