package com.postco.cacheservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.db.SelectRedisDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractRedisCommandService<T> implements CommandService<T> {
    protected final ReactiveRedisTemplate<String, Object> redisTemplate;
    protected final ObjectMapper objectMapper;

    protected abstract String getKeyPrefix();
    protected abstract Class<T> getEntityClass();
    public abstract int getRedisDatabase();

    @Override
    public Mono<Boolean> saveData(T data) {
        ReactiveHashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> map = objectMapper.convertValue(data, new TypeReference<>() {});
        Map<String, String> dataMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));

        String id = getIdFromData(data);
        String key = getKeyPrefix() + id;
        return hashOperations.putAll(key, dataMap).thenReturn(true);
    }

    @SelectRedisDatabase(database = "#this.getRedisDatabase()")
    @Override
    public Mono<Boolean> updateData(String id, T data) {
        return saveData(data);
    }

    @SelectRedisDatabase(database = "#this.getRedisDatabase()")
    @Override
    public Mono<Boolean> deleteData(String id) {
        return redisTemplate.delete(getKeyPrefix() + id).map(result -> result > 0);
    }

    private String getIdFromData(T data) {
        try {
            Method getIdMethod = getEntityClass().getMethod("getId");
            Long id = (Long) getIdMethod.invoke(data);
            return String.valueOf(id);  // Long 타입을 String으로 변환
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ID from data", e);
        }
    }
}
