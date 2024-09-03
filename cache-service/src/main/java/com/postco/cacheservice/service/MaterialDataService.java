package com.postco.cacheservice.service;

import com.postco.cacheservice.entity.Materials;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MaterialDataService {
    private final ReactiveRedisOperations<String, Materials> materialsOps;
    private final ReactiveRedisTemplate<String, String> stringRedisTemplate;
    private static final String MATERIAL_KEY_PREFIX = "material:";

    public Flux<Materials> getAllMaterials() {
        return materialsOps.keys(MATERIAL_KEY_PREFIX + "*")
                .flatMap(materialsOps.opsForValue()::get);
    }

    public Mono<Materials> getMaterialById(String id) {
        return materialsOps.opsForValue().get(MATERIAL_KEY_PREFIX + id);
    }

    public Mono<Boolean> saveMaterials(Materials material) {
        return materialsOps.opsForValue().set(MATERIAL_KEY_PREFIX + material.getId(), material)
                .flatMap(result -> stringRedisTemplate.convertAndSend("material-updates", "Material updated: " + material.getId())
                        .thenReturn(result));
    }
    public Mono<Boolean> deleteMaterial(String id) {
        return materialsOps.opsForValue().delete(MATERIAL_KEY_PREFIX + id)
                .flatMap(result -> stringRedisTemplate.convertAndSend("material-updates", "Material deleted: " + id)
                        .thenReturn(result));
    }
}
