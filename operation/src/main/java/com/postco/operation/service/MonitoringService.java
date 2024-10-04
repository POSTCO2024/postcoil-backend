package com.postco.operation.service;

import com.postco.operation.presentation.dto.WorkScheduleSummaryDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MonitoringService {
    Mono<List<WorkScheduleSummaryDTO>> getWorkScheduleSummary();
}
