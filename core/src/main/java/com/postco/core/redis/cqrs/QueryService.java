package com.postco.core.redis.cqrs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface QueryService<T> {
    Mono<T> getData(String id);
    Flux<T> getAllData();
    // 저장된 ID 존재여부 확인
    public Mono<Map<String, Boolean>> checkProcessedIdList(List<String> ids);

}
