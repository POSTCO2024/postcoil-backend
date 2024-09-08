package com.postco.core.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenericRedisDataManager<T, ID> implements RedisDataManager<T, ID>, KeyIndexManager<ID> {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;
    private final Class<T> entityClass;

    // CRUD 구현
    @Override
    public Mono<Void> save(T entity) {
        String key = generateKey(getEntityId(entity));
        return redisTemplate.opsForHash().putAll(key, convertToMap(entity)).then();
    }

    @Override
    public Mono<T> findById(ID id) {
        String key = generateKey(id);
        return redisTemplate.opsForHash().entries(key)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(map -> objectMapper.convertValue(map, entityClass));
    }

    @Override
    public Mono<Void> update(T entity) {
        return save(entity);  // Redis에서는 update가 save와 동일한 작업
    }

    @Override
    public Mono<Void> deleteById(ID id) {
        String key = generateKey(id);
        return redisTemplate.delete(key).then();
    }

    // 키 및 보조 인덱스 관리 구현
    @Override
    public String generateKey(ID id) {
        return keyPrefix + ":" + id;
    }

    @Override
    public Mono<Void> addToIndex(String indexKey, ID id) {
        return redisTemplate.opsForSet().add(indexKey, id.toString()).then();
    }

    @Override
    public Flux<ID> getIdFromIndex(String indexKey) {
        return null;
    }

    @Override
    public Mono<Void> removeFromIndex(String indexKey, ID id) {
        return redisTemplate.opsForSet().remove(indexKey, id.toString()).then();
    }

    // 엔티티에서 ID 추출 (리플렉션 사용)
    protected ID getEntityId(T entity) {
        try {
            return (ID) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get entity ID", e);
        }
    }

    // 엔티티 객체를 Map으로 변환
    private Map convertToMap(T entity) {
        return objectMapper.convertValue(entity, Map.class);
    }
}
