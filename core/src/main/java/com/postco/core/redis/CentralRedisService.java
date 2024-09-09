package com.postco.core.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CentralRedisService {
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;

    public <T> Mono<T> getData(String key, Class<T> entityClass) {
        return reactiveRedisTemplate.opsForHash().entries(key)
                .collectMap(entry -> (String) entry.getKey(), Map.Entry::getValue)
                .map(map -> objectMapper.convertValue(map, entityClass));
    }

    public <T> Flux<T> getAllData(String keyPattern, Class<T> entityClass) {
        return reactiveRedisTemplate.keys(keyPattern)
                .flatMap(key -> reactiveRedisTemplate.opsForHash().entries(key)
                        .collectMap(entry -> (String) entry.getKey(), Map.Entry::getValue)
                        .map(map -> objectMapper.convertValue(map, entityClass)));
    }
    public Mono<Boolean> setData(String key, Object data) {
        Map<String, Object> map = objectMapper.convertValue(data, Map.class);
        return reactiveRedisTemplate.opsForHash().putAll(key, map);
    }
}