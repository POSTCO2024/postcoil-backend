package com.postco.operation.domain.repository;

import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WorkInstructionRepositoryCustom {
    List<ControlClientDTO.StatisticsInfo> getStatisticsInfo();
    Mono<List<AnalysisDashboardClientDTO.StatisticsInfo>> getAnlysisStaticsInfo(String SchProcess);
    List<AnalysisDashboardClientDTO.StatisticsInfo> getAnlysisAllStaticsInfo();
}
