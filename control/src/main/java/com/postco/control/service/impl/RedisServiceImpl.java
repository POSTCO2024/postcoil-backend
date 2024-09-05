package com.postco.control.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.control.service.RedisService;
import com.postco.core.common.redis.RedisDataService;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisDataService redisDataService;

    @Override
    public Mono<Void> getAllMaterialsFromRedis() {
        return redisDataService.getAllMaterialData()
                .flatMap(this::mappingMaterials)
                .then();
    }

    @Override
    public Mono<Void> getNewMaterialsFromRedis() {
        return redisDataService.getProcessedId()
                .flatMap(lastProcessedId -> redisDataService.getNewMaterialData(lastProcessedId)
                        .flatMap(this::mappingMaterials)
                        .then(updateLastProcessedId()))
                .then();
    }

    private Mono<List<MaterialDTO.View>> mappingMaterials(List<Map<Object, Object>> materials) {
        List<MaterialDTO.View> mapResult = materials.stream()
                .map(material -> new MaterialDTO.View().convert(MaterialDTO.View.class))
                .collect(Collectors.toList());

        return Mono.just(mapResult);
    }

    private Mono<Void> updateLastProcessedId() {
        return redisDataService.getAllMaterialData()
                .map(materials -> materials.stream()
                        .mapToLong(m -> Long.parseLong(m.get("id").toString()))
                        .max()
                        .orElse(0L))
                .flatMap(redisDataService::setProcessedId)
                .then();

    }
}
