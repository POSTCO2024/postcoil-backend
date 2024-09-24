package com.postco.operation.service;

import com.postco.operation.service.impl.RedisDirectServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RedisDirectServiceImpl redisDirectService;
    private final KafkaMessageService kafkaMessageService;

    private static final String OPERATION_INIT_KEY = "operation:data:initialized";

    /**
     * 모든 데이터 초기화 메서드
     * 1) Redis 데이터 초기화
     * 2) Kafka로 데이터 전송
     */
    public Mono<Void> initializeAllData() {
        return initializeRedisData();
//                .then(sendKafkaData());
    }

    /**
     * Redis 데이터 초기화 메서드
     * - 이미 초기화된 데이터가 있는지 확인
     * - 없다면 데이터 저장 후 초기화 키 설정
     */
    public Mono<Void> initializeRedisData() {
        return checkOperationDataInitialized()
                .flatMap(initialized -> {
                    if (initialized) {
                        log.info("[Redis 스킵] 이미 초기화된 데이터가 존재합니다.");
                        return Mono.empty();
                    } else {
                        log.info("[Redis 초기화] 데이터 초기화를 시작합니다.");
                        return saveAllData()
                                .flatMap(success -> {
                                    if (success) {
                                        return setOperationInitKey()
                                                .doOnSuccess(v -> log.info("[Redis 성공] 모든 데이터 초기화가 완료되었습니다."));
                                    } else {
                                        log.error("[Redis 실패] 일부 데이터 저장에 실패하여 초기화 키를 설정하지 않습니다.");
                                        return Mono.empty();
                                    }
                                });
                    }
                });
    }

    /**
     * 초기화 키 존재 여부 확인
     */
    private Mono<Boolean> checkOperationDataInitialized() {
        return redisTemplate.hasKey(OPERATION_INIT_KEY);
    }

    /**
     * 초기화 완료 키 설정
     */
    private Mono<Void> setOperationInitKey() {
        return redisTemplate.opsForValue().set(OPERATION_INIT_KEY, "true")
                .then(Mono.fromRunnable(() -> log.info("[Redis 성공] 조업 초기 데이터 키 셋팅 성공")));
    }

    /**
     * 모든 데이터 저장
     * 하나라도 실패하면 false 반환
     */
    private Mono<Boolean> saveAllData() {
        return Mono.when(
                        redisDirectService.saveColdStandardReductionData(),
                        redisDirectService.saveEquipmentData(),
                        redisDirectService.saveEquipmentStatus(),
                        redisDirectService.savePlanProcessData()
                )
                .then(Mono.just(true))
                .doOnSuccess(unused -> log.info("[Redis 성공] 모든 데이터가 성공적으로 저장되었습니다."))
                .onErrorResume(error -> {
                    log.error("[Redis 실패] 일부 데이터 저장에 실패했습니다.", error);
                    return Mono.just(false);
                });
    }

    /**
     * Kafka로 데이터 전송
     */
    private Mono<Void> sendKafkaData() {
        log.info("[Kafka 전송] 재료 및 주문 데이터 전송 시작...");
        return Mono.when(
                Mono.fromRunnable(kafkaMessageService::sendAllMaterials),
                Mono.fromRunnable(kafkaMessageService::sendOrders)
        ).then(Mono.fromRunnable(() -> log.info("[Kafka 완료] 카프카 데이터 전송 완료")));
    }
}