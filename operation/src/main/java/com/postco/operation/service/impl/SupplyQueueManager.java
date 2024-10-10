package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.CoilSupply;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.service.CoilSupplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplyQueueManager {
    private static final String EQUIPMENT_QUEUE_KEY_PREFIX = "equipmentQueue:";
    private static final String SUPPLY_REQUEST_QUEUE_KEY_PREFIX = "supplyRequestQueue:";
    private static final long DELAY_IN_SECONDS = 3L;

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final CoilSupplyService coilSupplyService;
    private final TransactionTemplate transactionTemplate;

    public Mono<Void> addItemsToQueue(String equipmentCode, Long workInstructionId, String supplyRequestId, List<WorkInstructionItem> items) {
        String equipmentQueueKey = EQUIPMENT_QUEUE_KEY_PREFIX + equipmentCode;
        String supplyRequestQueueKey = SUPPLY_REQUEST_QUEUE_KEY_PREFIX + equipmentCode + ":" + supplyRequestId;

        return redisTemplate.opsForList().rightPush(equipmentQueueKey, supplyRequestId)
                .then(Flux.fromIterable(items)
                        .flatMap(item -> redisTemplate.opsForList().rightPush(supplyRequestQueueKey, item.getId().toString())
                                .doOnSuccess(result -> log.info("[보급 요청 성공] 설비 {}, 작업지시 {}, 보급요청 ID {}: 큐에 작업 아이템 ID : {} 추가됨",
                                        equipmentCode, workInstructionId, supplyRequestId, item.getId()))
                                .doOnError(error -> log.error("[보급 요청 실패] 설비 {}, 작업지시 {}, 보급요청 ID {}: 큐 추가 중 오류 발생: {}",
                                        equipmentCode, workInstructionId, supplyRequestId, error.getMessage())))
                        .collectList())
                .then();
    }

    public Mono<Void> processSupplyInBackground(String equipmentCode, CoilSupply coilSupply, String supplyRequestId, int supplyCount, Function<Long, Mono<Long>> startWorkOnItem) {
        return completeSupplyAfterDelay(equipmentCode, coilSupply, supplyCount)
                .then(processSupplyRequest(equipmentCode, supplyRequestId, startWorkOnItem))
                .doOnSuccess(v -> log.info("설비 {}, 보급요청 ID {}: 작업 완료", equipmentCode, supplyRequestId))
                .doOnError(e -> log.error("설비 {}, 보급요청 ID {}: 작업 처리 중 오류 발생", equipmentCode, supplyRequestId, e));
    }

    private Mono<Void> completeSupplyAfterDelay(String equipmentCode, CoilSupply coilSupply, int suppliedCount) {
        return Mono.delay(Duration.ofSeconds(DELAY_IN_SECONDS))
                .then(Mono.fromCallable(() ->
                        transactionTemplate.execute(status ->
                                coilSupplyService.updateCoilSupply(coilSupply.getWorkInstruction().getId(), suppliedCount))
                ).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(updated -> {
                    if (Boolean.TRUE.equals(updated)) {
                        log.info("설비 {}: 보급 완료된 코일 수: {}", equipmentCode, suppliedCount);
                        return Mono.empty();
                    } else {
                        return Mono.error(new RuntimeException("코일 보급 업데이트 실패"));
                    }
                });
    }

    private Mono<Void> processSupplyRequest(String equipmentCode, String supplyRequestId, Function<Long, Mono<Long>> startWorkOnItem) {
        String supplyRequestQueueKey = SUPPLY_REQUEST_QUEUE_KEY_PREFIX + equipmentCode + ":" + supplyRequestId;
        return processAllItemsInQueue(supplyRequestQueueKey, startWorkOnItem)
                .then(redisTemplate.opsForList().leftPop(EQUIPMENT_QUEUE_KEY_PREFIX + equipmentCode))
                .then();
    }

    private Mono<Void> processAllItemsInQueue(String queueKey, Function<Long, Mono<Long>> startWorkOnItem) {
        return redisTemplate.opsForList().leftPop(queueKey)
                .flatMap(itemId -> Mono.justOrEmpty(Long.parseLong(itemId)))
                .flatMap(id -> startWorkOnItem.apply(id)
                        .doOnSuccess(startedId -> log.info("작업 아이템 ID 처리 시작: {}", startedId))
                        .doOnError(e -> log.error("작업 아이템 ID {} 처리 중 오류 발생", id, e)))
                .repeat()
                .takeUntil(Objects::isNull)
                .then();
    }
}
