package com.postco.core.redis.cqrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.db.SelectRedisDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractRedisQueryService<T> implements QueryService<T> {
    protected final ReactiveRedisTemplate<String, Object> redisTemplate;
    protected final ObjectMapper objectMapper;

    protected abstract String getKeyPrefix();
    protected abstract Class<T> getEntityClass();
    public abstract int getRedisDatabase();

    @Override
    public Mono<T> getData(String id) {
        String key = getKeyPrefix() + id;
        return redisTemplate.opsForHash().entries(key)
                .collectMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())
                .map(this::convertMapToEntity);
    }

    @Override
    public Flux<T> getAllData() {
        return redisTemplate.keys(getKeyPrefix() + "*")
                .flatMap(key -> redisTemplate.opsForHash().entries(key)
                        .collectMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())
                        .map(this::convertMapToEntity));
    }

    @Override
    public Mono<Map<String, Boolean>> checkProcessedIdList(List<String> ids) {
        String processedSetKey = getProcessedIdsKey();
        return Flux.fromIterable(ids)
                .flatMap(id -> redisTemplate.opsForSet().isMember(processedSetKey, id)
                        .map(isMember -> Map.entry(id, isMember != null && isMember))
                        .defaultIfEmpty(Map.entry(id, false))) // null일 경우 false 처리
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    protected String getProcessedIdsKey() {
        return "processed_idSet:" + getKeyPrefix();
    }

    private T convertMapToEntity(Map<String, String> map) {
        return objectMapper.convertValue(map, getEntityClass());
    }

}
