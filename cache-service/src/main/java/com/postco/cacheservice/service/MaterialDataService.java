package com.postco.cacheservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.entity.Materials;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialDataService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final String MATERIAL_KEY_PREFIX = "material:";

    public Mono<Boolean> saveMaterials(Materials material) {
        ReactiveHashOperations<String, String, String> hashOperations = reactiveRedisTemplate.opsForHash();

        Map<String, Object> map = objectMapper.convertValue(material, new TypeReference<>() {});
        Map<String, String> materialMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));

        String key = MATERIAL_KEY_PREFIX + material.getId();
        return hashOperations.putAll(key, materialMap);
    }

    public Mono<Materials> getMaterials(String id) {
        ReactiveHashOperations<String, String, String> hashOperations = reactiveRedisTemplate.opsForHash();
        String key = MATERIAL_KEY_PREFIX + id;

        return hashOperations.entries(key)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(map -> objectMapper.convertValue(map, Materials.class));
    }
}
