package com.postco.control.service.impl.redis;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.RedisDataContainer;
import com.postco.core.dto.TargetMaterialDTO;
import com.postco.core.redis.cqrs.query.GenericRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 작업대상재로 등록된 재료들의 정보만 Redis 에서 가져오는 메소드 입니다.
     * 외부 사용이 필요한 경우, interface 에서 명시하고 public 으로 바꾼 뒤, override 하세요.
     */
    private Mono<RedisDataContainer> fetchRelatedMaterials(List<TargetMaterialDTO.View> targetMaterials) {
        // materialId 목록 추출
        List<String> materialIds = targetMaterials.stream()
                .map(TargetMaterialDTO.View::getMaterialId)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.toList());

        // 재료 데이터를 Redis에서 가져옴
        return redisQueryService.fetchByKeysInSinglePrefix("material:", materialIds, MaterialDTO.View.class)
                .collectList()
                .map(materials -> RedisDataContainer.builder()
                        .materials(materials)
                        .targetMaterials(targetMaterials)
                        .build())
                .doOnNext(container -> log.info("[Redis 성공] 연관된 재료 데이터를 Redis로부터 불러왔습니다. 개수: {}", container.getMaterials().size()));
    }

    private Mono<RedisDataContainer> fetchRelatedOrders(RedisDataContainer container) {
        // orderId 목록 추출
        List<String> orderIds = container.getMaterials().stream()
                .map(MaterialDTO.View::getOrderId)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.toList());

        // 주문 데이터를 Redis에서 가져옴
        return redisQueryService.fetchByKeysInSinglePrefix("order:", orderIds, OrderDTO.View.class)
                .collectList()
                .map(orders -> {
                    container.setOrders(orders);
                    return container;
                })
                .doOnNext(c -> log.info("[Redis 성공] 연관된 주문 데이터를 Redis 로부터 불러왔습니다. 주문 개수: {}", c.getOrders().size()));
    }
}
