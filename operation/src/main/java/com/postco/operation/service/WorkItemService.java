package com.postco.operation.service;

import reactor.core.publisher.Mono;

public interface WorkItemService {
    // 리젝 처리
    boolean rejectWorkItem(Long itemId);

    // 작업 시작 업데이트
    Mono<Boolean> startWorkItem(Long itemId);

    // 작업 종료 업데이트
    Mono<Boolean> finishWorkItem(Long itemId);
}
