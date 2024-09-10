package com.postco.core.redis.cqrs.temp.impl;

import com.postco.core.redis.cqrs.temp.KeyIndexManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RequiredArgsConstructor
public class KeyIndexManagerImpl<ID> implements KeyIndexManager<ID> {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final String keyPrefix;

    @Override
    public String generateKey(ID id) {
        return keyPrefix + ":" + id.toString();
    }

    @Override
    public Mono<Void> addToIndex(String indexKey, ID id) {
        return redisTemplate.opsForSet().add(indexKey, generateKey(id)).then();
    }

    @Override
    public Flux<ID> getIdFromIndex(String indexKey) {
        return redisTemplate.opsForSet().members(indexKey)
                .map(key -> {
                    // keyPrefix를 제거하고 ID 추출
                    String idStr = key.toString().substring(keyPrefix.length() + 1);  // +1 for ':'
                    return (ID) idStr;  // 타입 변환 (필요시 처리)
                });
    }

    @Override
    public Mono<Void> removeFromIndex(String indexKey, ID id) {
        return redisTemplate.opsForSet().remove(indexKey, generateKey(id)).then();
    }
}
