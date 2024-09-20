package com.postco.operation.service;

import com.postco.core.dto.ScheduleResultDTO;
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
}
