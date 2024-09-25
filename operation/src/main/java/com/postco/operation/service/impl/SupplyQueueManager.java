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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplyQueueManager {
    private static final String EQUIPMENT_QUEUE_KEY_PREFIX = "equipmentQueue:";
    private static final String WORK_INSTRUCTION_QUEUE_KEY_PREFIX = "workInstructionQueue:";
    private static final long DELAY_IN_SECONDS = 2L;                  // 설비 앞까지 오는 시간dt

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final CoilSupplyService coilSupplyService;
    private final TransactionTemplate transactionTemplate;

    public Mono<Void> addItemsToQueue(String equipmentCode, Long workInstructionId, List<WorkInstructionItem> items) {
        String equipmentQueueKey = EQUIPMENT_QUEUE_KEY_PREFIX + equipmentCode;
        String workInstructionQueueKey = WORK_INSTRUCTION_QUEUE_KEY_PREFIX + equipmentCode + ":" + workInstructionId;

        return redisTemplate.opsForList().rightPush(equipmentQueueKey, workInstructionId.toString())
                .then(Flux.fromIterable(items)
                        .flatMap(item -> redisTemplate.opsForList().rightPush(workInstructionQueueKey, item.getId().toString())
                                .doOnSuccess(result -> log.info("[보급 요청 성공] 설비 {}, 작업지시 {}: 큐에 작업 아이템 ID : {} 추가됨",
                                        equipmentCode, workInstructionId, item.getId()))
                                .doOnError(error -> log.error("[보급 요청 실패] 설비 {}, 작업지시 {}: 큐 추가 중 오류 발생: {}",
                                        equipmentCode, workInstructionId, error.getMessage())))
                        .collectList())
                .then();
    }

    public Mono<Void> processSupplyInBackground(String equipmentCode, CoilSupply coilSupply, int supplyCount, Function<Long, Mono<Long>> startWorkOnItem) {
        return Mono.defer(() -> {
            processCoilSupply(equipmentCode, coilSupply, supplyCount, startWorkOnItem);
            startWorkInstructionProcessing(equipmentCode, startWorkOnItem);
            return Mono.empty();
        });
    }

    private void processCoilSupply(String equipmentCode, CoilSupply coilSupply, int supplyCount, Function<Long, Mono<Long>> startWorkOnItem) {
        Mono.delay(Duration.ofSeconds(DELAY_IN_SECONDS))
                .then(completeSupplyAfterDelay(equipmentCode, coilSupply, supplyCount, startWorkOnItem))
                .doOnSuccess(v -> log.info("설비 {}: 백그라운드 보급 처리 완료", equipmentCode))
                .doOnError(error -> log.error("설비 {}: 백그라운드 보급 처리 중 오류 발생", equipmentCode, error))
                .subscribe();
    }

    private void startWorkInstructionProcessing(String equipmentCode, Function<Long, Mono<Long>> startWorkOnItem) {
        processNextWorkInstruction(equipmentCode, startWorkOnItem)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null,
                        error -> log.error("설비 {}: 작업 지시 처리 중 오류 발생", equipmentCode, error),
                        () -> log.info("설비 {}: 모든 작업 지시 처리 완료", equipmentCode)
                );
    }

    private Mono<Boolean> processNextWorkInstruction(String equipmentCode, Function<Long, Mono<Long>> startWorkOnItem) {
        String equipmentQueueKey = EQUIPMENT_QUEUE_KEY_PREFIX + equipmentCode;
        return redisTemplate.opsForList().leftPop(equipmentQueueKey)
                .flatMap(workInstructionId -> {
                    String workInstructionQueueKey = WORK_INSTRUCTION_QUEUE_KEY_PREFIX + equipmentCode + ":" + workInstructionId;
                    return processWorkInstructionItems(equipmentCode, workInstructionQueueKey, startWorkOnItem)
                            .then(checkAndProcessRemainingItems(equipmentQueueKey, workInstructionQueueKey, workInstructionId))
                            .thenReturn(true);
                })
                .defaultIfEmpty(false)
                .doOnNext(hasMore -> {
                    if (!hasMore) {
                        log.info("설비 {}: 처리할 작업 지시가 더 이상 없습니다.", equipmentCode);
                    }
                });
    }

    private Mono<Void> checkAndProcessRemainingItems(String equipmentQueueKey, String workInstructionQueueKey, String workInstructionId) {
        return redisTemplate.opsForList().size(workInstructionQueueKey)
                .flatMap(size -> Mono.just(size)
                        .filter(s -> s == 0)
                        .flatMap(s -> {
                            log.info("작업 지시 {} 의 모든 아이템 처리 완료", workInstructionId);
                            return Mono.empty();
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            log.info("작업 지시 {} 에 처리할 아이템이 {} 개 남아있음", workInstructionId, size);
                            return redisTemplate.opsForList().rightPush(equipmentQueueKey, workInstructionId);
                        }))
                )
                .then();
    }

    private Mono<Void> processWorkInstructionItems(String equipmentCode, String workInstructionQueueKey, Function<Long, Mono<Long>> startWorkOnItem) {
        return redisTemplate.opsForList().leftPop(workInstructionQueueKey)
                .flatMap(itemId -> Mono.justOrEmpty(Long.parseLong(itemId)))
                .flatMap(startWorkOnItem)
                .doOnNext(id -> log.info("설비 {}: 작업 아이템 ID 처리 완료: {}", equipmentCode, id))
                .then();
    }

    private Mono<Void> completeSupplyAfterDelay(String equipmentCode, CoilSupply coilSupply, int suppliedCount, Function<Long, Mono<Long>> startWorkOnItem) {
        return Mono.delay(Duration.ofSeconds(DELAY_IN_SECONDS))
                .then(Mono.fromCallable(() ->
                        transactionTemplate.execute(status -> {
                            boolean updated = coilSupplyService.updateCoilSupply(coilSupply.getWorkInstruction().getId(), suppliedCount);
                            if (!updated) {
                                throw new RuntimeException("코일 보급 업데이트 실패");
                            }
                            return true;
                        })
                ).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(updated -> processQueuedItems(equipmentCode, suppliedCount, startWorkOnItem))
                .doOnSuccess(v -> log.info("설비 {}: 보급 완료된 코일 수: {}", equipmentCode, coilSupply.getSuppliedCoils()))
                .doOnError(error -> log.error("설비 {}: 보급 처리 중 오류 발생: {}", equipmentCode, error.getMessage()))
                .then();
    }

    private Mono<Void> processQueuedItems(String equipmentCode, int count, Function<Long, Mono<Long>> startWorkOnItem) {
        String workInstructionQueueKey = WORK_INSTRUCTION_QUEUE_KEY_PREFIX + equipmentCode;
        return Flux.range(0, count)
                .flatMap(i -> redisTemplate.opsForList().leftPop(workInstructionQueueKey)
                        .flatMap(idString -> Mono.justOrEmpty(Long.parseLong(idString)))
                        .flatMap(startWorkOnItem)
                        .doOnNext(id -> log.info("설비 {}: 큐에서 작업 아이템 ID 처리: {}", equipmentCode, id))
                        .switchIfEmpty(Mono.fromRunnable(() -> log.info("설비 {}: 더 이상 처리할 아이템이 없습니다.", equipmentCode))))
                .then();
    }
}
