package com.postco.operation.service;

import com.postco.core.dto.ScheduleResultDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
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

    /**
     * 공정코드로만 작업지시서 조회
     */
    public Mono<List<WorkInstructionDTO.View>> getUncompletedWorkInstructions(String process);

    /**
     * 추가) Sohyun Ahn 240930,
     * websocket전 작업지시서와 코일 공급 통계 반환
     */
    public Mono<List<ClientDTO>> getUncompletedWorkInstructionsBeforeWebSocket(String process);

    /**
     * 끝나지 않은 작업지시서 조회
     */
    public Mono<List<WorkInstructionDTO.View>> getCompletedWorkInstructions(String process);
}
