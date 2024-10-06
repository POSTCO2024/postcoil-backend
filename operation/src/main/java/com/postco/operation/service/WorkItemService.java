package com.postco.operation.service;

import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WorkItemService {
    // 리젝 처리
    boolean rejectWorkItem(Long itemId);

    // 작업 시작 업데이트
    Mono<WorkInstructionItem> startWorkItem(Long itemId);

    // 작업 종료 업데이트
    Mono<Boolean> finishWorkItem(Long itemId);

    Mono<List<WorkInstructionItemDTO.SimulationItemDTO>> getWorkItems(Long workInstructionId);
}
