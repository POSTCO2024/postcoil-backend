package com.postco.control.service.impl;

import com.postco.control.service.RedisService;
import com.postco.core.redis.service.RedisDataService;
import com.postco.core.dto.DTO;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisDataService redisDataService;

    @Override
    public Mono<List<MaterialDTO.View>> getAllMaterialsFromRedis() {
        return redisDataService.getAllMaterialData()
                .flatMap(this::mappingMaterials)
                .doOnNext(materials -> log.info("[성공] 모든 재료들을 Redis 로부터 불러왔습니다 : {}", materials));
    }

    @Override
    public Mono<List<MaterialDTO.View>> getNewMaterialsFromRedis() {
        return redisDataService.getProcessedId()
                .flatMap(lastProcessedId -> redisDataService.getNewMaterialData(lastProcessedId)
                        .flatMap(this::mappingMaterials)
                        .flatMap(materials -> updateLastProcessedId().thenReturn(materials)))
                .doOnSuccess(materials -> log.info("[성공] 새롭게 추가된 재료를 Redis 로부터 가져옴: {}", materials));

    }

    private Mono<List<MaterialDTO.View>> mappingMaterials(List<Map<Object, Object>> materials) {
        List<MaterialDTO.View> mapResult = materials.stream()
                .map(material -> DTO.fromMap(material, MaterialDTO.View.class))
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
