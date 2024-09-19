package com.postco.core.redis;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CommandService<T> {
    Mono<Boolean> saveData(T data);
    // 처리된 ID를 Set에 추가
    Mono<Boolean> addProcessedId(String id);
    // 저장된 idSet 확인
    Mono<Map<String, Boolean>> checkProcessedIdSet(List<String> idSet);
    Mono<Boolean> updateData(String id, T data);
    Mono<Boolean> deleteData(String id);
}
