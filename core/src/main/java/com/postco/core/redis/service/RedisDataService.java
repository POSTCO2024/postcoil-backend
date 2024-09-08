package com.postco.core.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisDataService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /**
     * 모든 Material 데이터를 가져오는 메서드
     * @return 모든 재료 데이터를 List 형태로 반환
     */
    public Mono<List<Map<Object, Object>>> getAllMaterialData() {
        return redisTemplate.keys("material:*")
                .flatMap(key -> redisTemplate.opsForHash().entries(key)
                        .collectMap(Map.Entry::getKey, Map.Entry::getValue))
                .collectList();
    }

    /**
     * Redis 에서 새로 추가된 데이터 부터 가져오는 메서드
     * @param lastProcessedId 마지막으로 처리한 재료 ID
     * @return 새로 추가된 재료 데이터 List 형태로 반환
     */
    public Mono<List<Map<Object, Object>>> getNewMaterialData(Long lastProcessedId) {
        return redisTemplate.keys("material:*")
                .flatMap(key -> redisTemplate.opsForHash().entries(key)
                        .collectMap(Map.Entry::getKey, Map.Entry::getValue))
                .filter(material -> Long.parseLong(material.get("id").toString()) > lastProcessedId)
                .collectList();
    }

    public Mono<Long> getProcessedId() {
        return redisTemplate.opsForValue().get("last_processed_id")
                .map(Long::parseLong)
                .defaultIfEmpty(0L);
    }

    public Mono<Boolean> setProcessedId(Long id) {
        return redisTemplate.opsForValue().set("last_processed_id", id.toString());
    }
}
