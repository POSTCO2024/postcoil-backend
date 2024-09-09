package com.postco.control.service.impl;

import com.postco.control.service.RedisService;
import com.postco.core.dto.DTO;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.redis.CentralRedisService;
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
    private final CentralRedisService centralRedisService;

    @Override
    public Mono<List<MaterialDTO.View>> getAllMaterialsFromRedis() {
        return centralRedisService.getAllData("material:*", MaterialDTO.View.class)
                .collectList()  // Flux -> Mono<List<MaterialDTO.View>>
                .doOnNext(materials -> log.info("[성공] 모든 재료들을 Redis로부터 불러왔습니다: {}", materials));
    }

    @Override
    public Mono<List<MaterialDTO.View>> getNewMaterialsFromRedis() {
        return centralRedisService.getData("last_processed_id", Long.class)
                .defaultIfEmpty(0L)
                .flatMap(lastProcessedId ->
                        centralRedisService.getAllData("material:*", MaterialDTO.View.class)
                                .filter(material -> Long.parseLong(String.valueOf(material.getId())) > lastProcessedId)
                                .collectList()
                                .flatMap(materials -> updateLastProcessedId().thenReturn(materials)))
                .doOnSuccess(materials -> log.info("[성공] 새롭게 추가된 재료를 Redis 로부터 가져옴: {}", materials));
    }

    @Override
    public Mono<List<OrderDTO.View>> getAllOrders() {
        return centralRedisService.getAllData("order:*", OrderDTO.View.class)
                .collectList()
                .doOnNext(orders -> log.info("[성공] 모든 주문들을 Redis로부터 불러왔습니다: {}", orders));
    }

    private Mono<Void> updateLastProcessedId() {
        return centralRedisService.getAllData("material:*", MaterialDTO.View.class)
                .map(material -> Long.parseLong(String.valueOf(material.getId())))
                .reduce(Math::max)
                .flatMap(maxId -> centralRedisService.setData("last_processed_id", maxId))
                .then();
    }

    public Mono<MaterialDTO.View> getMaterialById(Long materialId) {
        return centralRedisService.getData("material:" + materialId, MaterialDTO.View.class)
                .doOnSuccess(material -> log.info("[성공] Redis로부터 Material ID {}에 대한 데이터를 가져왔습니다: {}", materialId, material));
    }
}
