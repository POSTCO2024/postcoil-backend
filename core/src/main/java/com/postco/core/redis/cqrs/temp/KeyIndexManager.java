package com.postco.core.redis.cqrs.temp;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KeyIndexManager<ID> {
    /**
     * 주어진 id 값에 대한 Redis key 생성
     */
    String generateKey(ID id);
    // 보조 인덱스 추가
    Mono<Void> addToIndex(String indexKey, ID id);
    // 툭정 인덱스 키에 대한 ID 조회
    Flux<ID> getIdFromIndex(String indexKey);
    // 특정 인덱스에서 ID 제거
    Mono<Void> removeFromIndex(String indexKey, ID id);

}
