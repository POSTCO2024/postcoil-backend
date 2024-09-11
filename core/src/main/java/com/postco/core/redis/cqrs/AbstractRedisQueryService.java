package com.postco.core.redis.cqrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.db.SelectRedisDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractRedisQueryService<T> implements QueryService<T> {
    protected final ReactiveRedisTemplate<String, Object> redisTemplate;
    protected final ObjectMapper objectMapper;

    protected abstract String getKeyPrefix();
    protected abstract Class<T> getEntityClass();
    public abstract int getRedisDatabase();

    @SelectRedisDatabase(database = "#this.getRedisDatabase()")
    @Override
    public Mono<T> getData(String id) {
        String key = getKeyPrefix() + id;
        return redisTemplate.opsForHash().entries(key)
                .collectMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())
                .map(this::convertMapToEntity);
    }

    @SelectRedisDatabase(database = "#this.getRedisDatabase()")
    @Override
    public Flux<T> getAllData() {
        return redisTemplate.keys(getKeyPrefix() + "*")
                .flatMap(key -> redisTemplate.opsForHash().entries(key)
                        .collectMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())
                        .map(this::convertMapToEntity));
    }

    private T convertMapToEntity(Map<String, String> map) {
        return objectMapper.convertValue(map, getEntityClass());
    }
}
