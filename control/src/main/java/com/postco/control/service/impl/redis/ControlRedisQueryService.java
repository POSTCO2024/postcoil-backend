package com.postco.control.service.impl.redis;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.RedisDataContainer;
import com.postco.core.redis.cqrs.query.GenericRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ControlRedisQueryService {
    private final GenericRedisQueryService redisQueryService;

    public Mono<RedisDataContainer> getRedisData() {
        return Mono.zip(
                getAllMaterialsFromRedis(),
                getAllOrdersFromRedis()
        ).map(tuple -> RedisDataContainer.builder()
                .materials(tuple.getT1())
                .orders(tuple.getT2())
                .build()
        ).doOnNext(container -> log.info("[Redis 성공] Redis 로부터 관련 데이터를 불러왔습니다: 재료 {}, 주문 {}",
                container.getMaterials().size(), container.getOrders().size()));
    }

    public Mono<List<MaterialDTO.View>> getAllMaterialsFromRedis() {
        return redisQueryService.fetchAllBySinglePrefix("material:", MaterialDTO.View.class)
                .collectList()
                .doOnNext(materials -> log.info("[성공] 모든 재료들을 Redis 로부터 불러왔습니다: {}", materials.size()));
    }

    public Mono<List<OrderDTO.View>> getAllOrdersFromRedis() {
        return redisQueryService.fetchAllBySinglePrefix("order:", OrderDTO.View.class)
                .collectList()
                .doOnNext(orders -> log.info("[성공] 모든 주문들을 Redis 로부터 불러왔습니다: {}", orders.size()));
    }
}
