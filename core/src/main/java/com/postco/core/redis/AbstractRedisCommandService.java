package com.postco.core.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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

        return hashOperations.putAll(key, dataMap)
                .then(addProcessedId(id))
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> addProcessedId(String id) {
        String processedSetKey = getProcessedIdsKey();
        return redisTemplate.opsForSet().add(processedSetKey, id)
                .doOnSuccess(result -> log.info("[Redis 성공] 처리된 ID 저장: {}", id))
                .doOnError(error -> log.error("[Redis 실패] 처리된 ID 저장 중 오류 발생: {}", id, error)).hasElement();

    }

    @Override
    public Mono<Map<String, Boolean>> checkProcessedIdSet(List<String> idSet) {
        String processedSetKey = getProcessedIdsKey();
        return Flux.fromIterable(idSet)
                .flatMap(id -> redisTemplate.opsForSet().isMember(processedSetKey, id)
                        .map(isMember -> Map.entry(id, isMember != null && isMember))
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    protected String getProcessedIdsKey() {
        return "processed_idSet:" + getKeyPrefix();
    }


    @Override
    public Mono<Boolean> updateData(String id, T data) {
        return saveData(data);
    }


    @Override
    public Mono<Boolean> deleteData(String id) {
        return redisTemplate.delete(getKeyPrefix() + id).map(result -> result > 0);
    }

    protected String getIdFromData(T data) {
        try {
            Method getIdMethod = getEntityClass().getMethod("getId");
            Long id = (Long) getIdMethod.invoke(data);
            return String.valueOf(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ID from data", e);
        }
    }
}
