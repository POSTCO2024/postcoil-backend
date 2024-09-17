package com.postco.control.service.impl;

import com.postco.control.domain.repository.ControlRedisRepository;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDataLoader {
    private final ControlRedisRepository controlRedisRepository;

    public Mono<Tuple2<List<MaterialDTO.View>, List<OrderDTO.View>>> loadRedisData() {
        return Mono.zip(
                controlRedisRepository.getAllMaterials(),
                controlRedisRepository.getAllOrders()
        ).doOnSuccess(tuple -> {
            List<MaterialDTO.View> materials = tuple.getT1();
            List<OrderDTO.View> orders = tuple.getT2();

            log.info("[Redis 성공] Redis Service 에서 {} 개의 재료와 {} 개의 주문을 로드했습니다.",
                    materials.size(), orders.size());

            log.info("[재료 데이터] 최대 3개의 재료를 출력합니다:");
            materials.stream().limit(3).forEach(material -> {
                log.info("{}", material);
            });

            log.info("[주문 데이터] 최대 3개의 주문을 출력합니다:");
            orders.stream().limit(3).forEach(order -> {
                log.info("{}", order);
            });

        }).doOnError(error -> {
            log.error("[Redis 실패] Redis Service 에서 데이터를 로드하는 중 오류 발생: {}", error.getMessage(), error);
        });
    }
}
