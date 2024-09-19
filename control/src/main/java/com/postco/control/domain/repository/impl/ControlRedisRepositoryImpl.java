package com.postco.control.domain.repository.impl;

import com.postco.control.domain.repository.ControlRedisRepository;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.redis.cqrs.query.GenericRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ControlRedisRepositoryImpl implements ControlRedisRepository {
    private final GenericRedisQueryService queryService;

    // TODO: RedisKeyManager 로 관리하기
    private static final String MATERIAL_PREFIX = "material:";
    private static final String ORDER_PREFIX = "order:";

    @Override
    public Mono<List<MaterialDTO.View>> getAllMaterials() {
        return queryService.fetchAllBySinglePrefix(MATERIAL_PREFIX, MaterialDTO.View.class)
                .collectList()
                .doOnNext(materials -> log.info("[Redis 성공] ControlRepository 에서 모든 재료들을 Redis 로부터 불러왔습니다: {}", materials));
    }

    @Override
    public Mono<List<OrderDTO.View>> getAllOrders() {
        return queryService.fetchAllBySinglePrefix(ORDER_PREFIX, OrderDTO.View.class)
                .collectList()
                .doOnNext(orders -> log.info("[Redis 성공] ControlRepository 에서 모든 주문들을 Redis 로부터 불러왔습니다: {}", orders));
    }
}
