package com.postco.schedule.service.impl;

import com.postco.core.dto.*;
import com.postco.core.redis.cqrs.query.GenericRedisQueryService;
import com.postco.schedule.service.ScheduleRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleRedisQueryServiceImpl implements ScheduleRedisService {
    private final GenericRedisQueryService redisQueryService;

    @Override
    public Mono<RedisDataContainer> getScheduleData() {
        return fetchAllTargetsFromRedis()
                .flatMap(this::fetchRelatedMaterials)
                .flatMap(this::fetchRelatedOrders)
                .doOnNext(container -> log.info("[Redis 성공] Redis 로부터 작업대상재와 연관된 재료를 모두 불러왔습니다: {}", container));
    }

    @Override
    public Mono<RefDataContainer> getReferenceData() {
        return fetchEquipmentInfoFromRedis()
                .map(equipList -> RefDataContainer.builder()
                        .equipmentInfo(equipList)
                        .build()
                );
    }

    // 외부 사용이 필요한 경우, interface 에서 명시하고 public 으로 바꾼 뒤, override 하세요.
    private Mono<List<TargetMaterialDTO.View>> fetchAllTargetsFromRedis() {
        return redisQueryService.fetchAllBySinglePrefix("target:", TargetMaterialDTO.View.class)
                .collectList()
                .doOnNext(targets -> log.info("[Redis 성공] 모든 작업대상재를 Redis 로부터 불러왔습니다. 개수: {}", targets.size()));
    }

    private Mono<List<EquipmentInfoDTO.View>> fetchEquipmentInfoFromRedis() {
        return redisQueryService.fetchAllBySinglePrefix("equipment:", EquipmentInfoDTO.View.class)
                .collectList()
                .doOnNext(equipments -> log.info("[Redis 성공] 설비 관련 데이터를 Redis 로부터 불러왔습니다. 개수 : {}", equipments.size()));
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
