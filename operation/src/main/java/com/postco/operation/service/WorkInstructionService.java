package com.postco.operation.service;

import com.postco.core.dto.ScheduleResultDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WorkInstructionService {
    /**
     * 레디스로부터 확정 스케쥴 결과 가져오기
     */
    Mono<List<ScheduleResultDTO.View>> getConfirmedScheduleResults();

    /**
     * 스케쥴 서비스 API 호출하여 결과 가져오기
     */
    Mono<List<ScheduleResultDTO.View>> fetchFromOriginalApi();

    /**
     * 스케쥴 결과를 작업지시서 DTO로 매핑
     */
    List<WorkInstructionDTO.Create> mapScheduleResultsToWorkInstructions(List<ScheduleResultDTO.View> scheduleResults);

    /**
     * 작업지시서 DTO를 저장하고 성공 여부 반환
     */
    Mono<Boolean> saveWorkInstructions(List<WorkInstructionDTO.Create> workInstructions);

    /**
     * 작업지시서 조회
     */
    Mono<List<WorkInstructionDTO.View>> getWorkInstructions(String process, String rollUnit);
}
