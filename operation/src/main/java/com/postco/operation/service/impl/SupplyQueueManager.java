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
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplyQueueManager {
    private static final String SUPPLY_QUEUE_KEY = "supplyQueue";     // 큐 key
    private static final long DELAY_IN_SECONDS = 2L;                  // 설비 앞까지 오는 시간dt

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final CoilSupplyService coilSupplyService;
    private final TransactionTemplate transactionTemplate;

    public Mono<Void> addItemsToQueue(List<WorkInstructionItem> items, Function<Long, Mono<Long>> startWorkOnItem) {
        return Flux.fromIterable(items)
                .flatMap(item -> redisTemplate.opsForList().leftPush(SUPPLY_QUEUE_KEY, item.getId().toString())
                        .doOnSuccess(result -> log.info("[보급 요청 성공] 큐에 작업 아이템 ID : {} 추가됨", item.getId()))
                        .doOnError(error -> log.error("[보급 요청 실패] 큐 추가 중 오류 발생: {}", error.getMessage())))
                .then();
    }

    public void processSupplyInBackground(CoilSupply coilSupply, int supplyCount, Function<Long, Mono<Long>> startWorkOnItem) {
        Mono.delay(Duration.ofSeconds(DELAY_IN_SECONDS))
                .then(completeSupplyAfterDelay(coilSupply, supplyCount, startWorkOnItem))
                .doOnSuccess(v -> log.info("백그라운드 보급 처리 완료"))
                .doOnError(error -> log.error("백그라운드 보급 처리 중 오류 발생", error))
                .subscribe();
    }

    private Mono<Void> completeSupplyAfterDelay(CoilSupply coilSupply, int suppliedCount, Function<Long, Mono<Long>> startWorkOnItem) {
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
                .flatMap(updated -> processQueuedItems(suppliedCount, startWorkOnItem))
                .doOnSuccess(v -> log.info("보급 완료된 코일 수: {}", coilSupply.getSuppliedCoils()))
                .doOnError(error -> log.error("보급 처리 중 오류 발생: {}", error.getMessage()));
    }

    private Mono<Void> processQueuedItems(int count, Function<Long, Mono<Long>> startWorkOnItem) {
        return Flux.range(0, count)
                .flatMap(i -> redisTemplate.opsForList().leftPop(SUPPLY_QUEUE_KEY)
                        .flatMap(idString -> Mono.justOrEmpty(Long.parseLong(idString)))
                        .flatMap(startWorkOnItem)
                        .doOnNext(id -> log.info("큐에서 작업 아이템 ID 처리: {}", id))
                        .switchIfEmpty(Mono.fromRunnable(() -> log.info("더 이상 처리할 아이템이 없습니다."))))
                .then();
    }
}
