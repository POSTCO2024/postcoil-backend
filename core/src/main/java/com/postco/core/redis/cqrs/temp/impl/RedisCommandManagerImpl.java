package com.postco.core.redis.cqrs.temp.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.cqrs.temp.KeyIndexManager;
import com.postco.core.redis.cqrs.temp.RedisCommandManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import static org.springframework.cache.interceptor.SimpleKeyGenerator.generateKey;

@RequiredArgsConstructor
public class RedisCommandManagerImpl<T, ID> implements RedisCommandManager<T, ID> {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;
    private final Class<T> entityClass;
    private final KeyIndexManager<ID> keyIndexManager;

    @Override
    public Mono<Void> save(T entity) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(entity))
//                .flatMap(json -> redisTemplate.opsForValue().set(generateKey((ID)sgetIdFromEntity(entity)), json))
                .then();
    }

    @Override
    public Mono<Void> update(T entity) {
        return save(entity);
    }

    @Override
    public Mono<Void> deleteById(ID id) {
        return null;
    }
}
