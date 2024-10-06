package com.postco.operation.domain.repository;

import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface MaterialRepositoryCustom {
    List<ControlClientDTO.CurrentInfo> getCurrentInfo();
    Mono<List<AnalysisDashboardClientDTO.CurrentInfo>> getCurrentInfo(String SchProcess);
}
