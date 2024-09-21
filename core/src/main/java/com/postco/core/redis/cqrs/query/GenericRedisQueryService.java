package com.postco.core.redis.cqrs.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


/**
 * CQRS 패턴을 적용한 Redis Query 제네릭 서비스 클래스 입니다.
 * 해당 클래스는 "조회" 작업만을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericRedisQueryService implements QueryService {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 단일 Redis 키에 대한 비동기 데이터를 가져오는 메서드.
     * getData 로 대체
     * @param key Redis 키. prefix 가 결합된 완전한 Redis 키를 전달해야 합니다. (ex. material:1 )
     */
    @Override
    public <T> Mono<T> fetchByKeyFromRedis(String key, Class<T> entityClass) {
        return redisTemplate.opsForHash().entries(key)
                .collectMap(entry -> (String) entry.getKey(), Map.Entry::getValue)
                .map(map -> objectMapper.convertValue(map, entityClass))
                .switchIfEmpty(Mono.empty());
    }

    /**
     * 단일 프리픽스 내의 모든 데이터를 비동기로 가져오는 메서드.
     * getAllData 로 대체
     * 예 ) 재료 데이터만 가져오고 싶은 경우, prefix 로 material: 전달
     */
    @Override
    public <T> Flux<T> fetchAllBySinglePrefix(String prefix, Class<T> entityClass) {
        return redisTemplate.keys(prefix + "*")
                .flatMap(key -> redisTemplate.opsForHash().entries(key)
                        .collectMap(entry -> (String) entry.getKey(), Map.Entry::getValue)
                        .map(map -> objectMapper.convertValue(map, entityClass)))
                .switchIfEmpty(Flux.empty());
    }

    /**
     * 여러 프리픽스에 해당하는 데이터를 비동기로 가져오는 메서드.
     * 예 ) 재료 및 주문 데이터를 모두 가져오고 싶은 경우, 리스트로 전달.
     */
    @Override
    public <T> Flux<T> fetchAllByMultiplePrefixes(List<String> prefixes, Class<T> entityClass) {
        return Flux.fromIterable(prefixes)
                .flatMap(prefix -> redisTemplate.keys(prefix + "*")
                        .flatMap(key -> redisTemplate.opsForHash().entries(key)
                                .collectMap(entry -> (String) entry.getKey(), Map.Entry::getValue)
                                .map(map -> objectMapper.convertValue(map, entityClass))))
                .switchIfEmpty(Flux.empty());
    }

    /**
     * 특정 프리픽스 내에서 지정된 키들의 데이터를 비동기로 가져오는 메서드.
     * 예 ) 재료에서 해당하는 id 값의 데이터만 가져오고 싶은 경우 (material:1, 5, 16, 등등 이런식으로)
     */
    @Override
    public <T> Flux<T> fetchByKeysInSinglePrefix(String prefix, List<String> keys, Class<T> entityClass) {
        return Flux.fromIterable(keys)
                .flatMap(key -> redisTemplate.opsForHash().entries(prefix + key)
                        .collectMap(entry -> (String) entry.getKey(), Map.Entry::getValue)
                        .map(map -> objectMapper.convertValue(map, entityClass)))
                .switchIfEmpty(Flux.empty());
    }

    /**
     * 여러 프리픽스와 그에 해당하는 특정 키들의 데이터를 비동기로 가져오는 메서드.
     * 예 ) 재료 및 주문 데이터를 함께 가져오고 싶은 경우, material:1, order:5 이런식으로 여러 prefix 에 걸친 키들 조회
     */
    @Override
    public <T> Flux<T> fetchByKeysInMultiplePrefixes(Map<String, List<String>> prefixKeysMap, Class<T> entityClass) {
        return Flux.fromIterable(prefixKeysMap.entrySet())
                .flatMap(entry -> {
                    String prefix = entry.getKey();
                    List<String> keys = entry.getValue();
                    return Flux.fromIterable(keys)
                            .flatMap(key -> redisTemplate.opsForHash().entries(prefix + key)
                                    .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                                    .map(map -> objectMapper.convertValue(map, entityClass)));
                })
                .switchIfEmpty(Flux.empty());
    }

    /**
     * 처리된 ID 리스트를 확인하는 메서드.
     * @param idList 확인할 ID 리스트
     * @return 처리된 ID 여부를 담은 Map
     */
    @Override
    public Mono<Map<String, Boolean>> checkProcessedIdList(List<String> idList, String processedSetKey) {
        return Flux.fromIterable(idList)
                .flatMap(id -> redisTemplate.opsForSet().isMember(processedSetKey, id)
                        .map(isMember -> Map.entry(id, isMember != null && isMember))
                        .defaultIfEmpty(Map.entry(id, false)))  // Set에 포함되지 않은 경우 false 처리
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public <T, U> Flux<T> fetchAllBySinglePrefixWithJsonField(
            String prefix,
            Class<T> entityClass,
            String jsonField,
            TypeReference<U> jsonFieldType) {
        return redisTemplate.keys(prefix + "*")
                .flatMap(key -> redisTemplate.opsForHash().entries(key)
                        .collectMap(entry -> (String) entry.getKey(), Map.Entry::getValue)
                        .map(map -> {
                            String jsonString = (String) map.get(jsonField);
                            try {
                                U jsonObject = objectMapper.readValue(jsonString, jsonFieldType);
                                map.put(jsonField, jsonObject);
                            } catch (Exception e) {
                                log.error("Error parsing JSON field: {}", jsonField, e);
                            }
                            return objectMapper.convertValue(map, entityClass);
                        }))
                .switchIfEmpty(Flux.empty());
    }
}