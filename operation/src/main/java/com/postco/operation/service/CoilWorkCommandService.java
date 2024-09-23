package com.postco.operation.service;

import com.postco.operation.domain.entity.*;
import com.postco.operation.domain.repository.CoilSupplyRepository;
import com.postco.operation.domain.repository.WorkInstructionRepository;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.service.util.WorkSimulationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoilWorkCommandService {

    private static final long DELAY_IN_SECONDS = 2L;  // 설비 보급 완료 시간
    private static final String SUPPLY_QUEUE_KEY = "supplyQueue";  // Redis 큐 키

    private final WorkInstructionRepository workInstructionRepository;
    private final WorkItemRepository workItemRepository;
    private final WorkItemService workItemService;
    private final CoilSupplyRepository coilSupplyRepository;
    private final CoilSupplyService coilSupplyService;
    private final MaterialUpdateService materialUpdateService;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final TransactionTemplate transactionTemplate;

    // 작업 지시서에서 보급 요청 시 관련 작업 수행
    // 1) 코일 보급 처리
    // 2) 코일 작업 시작 및 종료
    public Mono<Boolean> requestSupply(Long workInstructionId, int supplyCount) {
        return Mono.fromCallable(() ->
                        transactionTemplate.execute(status -> {
                            WorkInstruction workInstruction = workInstructionRepository.findByIdWithItems(workInstructionId)
                                    .orElseThrow(() -> new IllegalArgumentException("Invalid workInstructionId"));

                            CoilSupply coilSupply = coilSupplyRepository.findByWorkInstruction(workInstruction)
                                    .orElseThrow(() -> new IllegalArgumentException("No CoilSupply found for the work instruction"));

                            log.info("작업 지시서 ID: {}, 보급 요청 수량: {}", workInstructionId, supplyCount);

                            List<WorkInstructionItem> itemsToQueue = workInstruction.getItems().stream()
                                    .filter(item -> item.getWorkItemStatus() == WorkStatus.PENDING)
                                    .sorted(Comparator.comparingInt(WorkInstructionItem::getSequence))
                                    .limit(supplyCount)
                                    .collect(Collectors.toList());

                            return new Object[]{itemsToQueue, coilSupply};
                        })
                ).subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    List<WorkInstructionItem> itemsToQueue = (List<WorkInstructionItem>) result[0];
                    CoilSupply coilSupply = (CoilSupply) result[1];
                    return addItemsToQueue(itemsToQueue)
                            .doOnSuccess(v -> processSupplyInBackground(coilSupply, itemsToQueue.size()))
                            .thenReturn(true);
                });
    }

    // 작업 아이템을 sequence 순서대로 큐에 추가
    private Mono<Void> addItemsToQueue(WorkInstruction workInstruction, int supplyCount) {
        List<WorkInstructionItem> pendingItems = workInstruction.getItems().stream()
                .filter(item -> item.getWorkItemStatus() == WorkStatus.PENDING)
                .sorted(Comparator.comparingInt(WorkInstructionItem::getSequence))
                .limit(supplyCount)
                .collect(Collectors.toList());

        // 보급할 작업 아이템이 없을 경우
        if (pendingItems.isEmpty()) {
            return Mono.error(new RuntimeException("더 이상 보급할 작업 아이템이 없습니다."));
        }

        return Flux.fromIterable(pendingItems)
                .flatMap(item -> redisTemplate.opsForList().leftPush(SUPPLY_QUEUE_KEY, item.getId().toString())
                        .doOnSuccess(result -> log.info("[보급 요청 성공] 큐에 작업 아이템 ID : {} 추가됨", item.getId()))
                        .doOnError(error -> log.error("[보급 요청 실패] 큐 추가 중 오류 발생: {}", error.getMessage())))
                .then(Mono.fromRunnable(() ->
                        log.info("[보급 요청 완료] 총 {}개의 작업 아이템이 큐에 추가됨. 작업 지시서 ID: {}", pendingItems.size(), workInstruction.getId())));
    }

    private void processSupplyInBackground(CoilSupply coilSupply, int supplyCount) {
        Mono.delay(Duration.ofSeconds(DELAY_IN_SECONDS))
                .then(completeSupplyAfterDelay(coilSupply, supplyCount))
                .doOnSuccess(v -> log.info("백그라운드 보급 처리 완료"))
                .doOnError(error -> log.error("백그라운드 보급 처리 중 오류 발생", error))
                .subscribe();
    }

    // 일정 시간 후 보급 완료 처리 및 큐에서 아이템 작업 시작
    private Mono<Void> completeSupplyAfterDelay(CoilSupply coilSupply, int suppliedCount) {
        return Mono.delay(Duration.ofSeconds(DELAY_IN_SECONDS))
                .then(Mono.fromCallable(() -> coilSupplyService.updateCoilSupply(coilSupply.getWorkInstruction().getId(), suppliedCount))
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(updated -> updated ? processQueuedItems(suppliedCount) : Mono.error(new RuntimeException("코일 보급 업데이트 실패")))
                .doOnSuccess(v -> log.info("보급 완료된 코일 수: {}", coilSupply.getSuppliedCoils()))
                .doOnError(error -> log.error("보급 처리 중 오류 발생: {}", error.getMessage()));
    }

    // 큐에서 작업아이템 처리
    private Mono<Void> processQueuedItems(int count) {
        return Flux.range(0, count)
                .flatMap(i -> redisTemplate.opsForList().rightPop(SUPPLY_QUEUE_KEY)
                        .flatMap(idString -> Mono.justOrEmpty(Long.parseLong(idString))) // String to Long 변환
                        .flatMap(this::startWorkOnItem)
                        .doOnNext(id -> log.info("큐에서 작업 아이템 ID 처리: {}", id))
                        .switchIfEmpty(Mono.fromRunnable(() -> log.info("더 이상 처리할 아이템이 없습니다."))))
                .then();
    }

    // step 2.
    // 보급 완료된 아이템 작업 시작
    private Mono<Long> startWorkOnItem(Long itemId) {
        return Mono.fromCallable(() -> workItemRepository.findById(itemId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalItem -> optionalItem
                        .map(item -> workItemService.startWorkItem(item.getId())
                                .then(scheduleWorkCompletion(item))
                                .thenReturn(itemId))
                        .orElseGet(() -> {
                            log.error("작업 시작 실패, 아이템 ID: {}", itemId);
                            return Mono.empty();
                        }));
    }

    private Mono<Void> scheduleWorkCompletion(WorkInstructionItem item) {
        long expectedDurationMinutes = item.getExpectedItemDuration();
        return completeWorkAfterDuration(item.getId(), item.getWorkInstruction().getId(), expectedDurationMinutes)
                .doOnSubscribe(s -> log.info("[작업 완료] 완료 프로세스 start... - 아이템 ID: {}", item.getId()))
                .doOnError(error -> log.error("[작업 완료] 완료 프로세스 에러 - 아이템 ID: {}, 에러: {}", item.getId(), error.getMessage()));
    }

    // step 3.
    // 일정 시간 이후 작업 종료, 1) 작업 아이템 업데이트 -> finishWorkItem
    // 2) 코일 보급 업데이트 -> updateFinishCount
    // 3) 재료 상태 업데이트 -> updateMaterialProgress, reduceThickAndWidth , updateProcess, updateYard
    public Mono<Void> completeWorkAfterDuration(Long itemId, Long workInstructionId, long expectedDurationMinutes) {
        long presentationDuration = WorkSimulationUtil.convertToPresentationDuration((int) expectedDurationMinutes);
        return WorkSimulationUtil.simulateWorkCompletion(presentationDuration)
                .then(finishWorkUpdates(itemId, workInstructionId))
                .doOnSuccess(v -> log.info("작업 완료 처리 성공 - 아이템 ID: {}", itemId))
                .doOnError(error -> log.error("작업 완료 처리 실패 - 아이템 ID: {}, 에러: {}", itemId, error.getMessage()));
    }

    private Mono<Void> finishWorkUpdates(Long itemId, Long workInstructionId) {
        return workItemService.finishWorkItem(itemId)
                .flatMap(success -> success ? updateCoilSupplyAndMaterial(workInstructionId) : Mono.error(new RuntimeException("작업 아이템 종료 업데이트 실패")))
                .doOnSuccess(v -> log.info("작업 아이템 업데이트 완료. ID: {}", itemId))
                .doOnError(e -> log.error("작업 아이템 종료 업데이트 실패. ID: {}", itemId));
    }

    private Mono<Void> updateCoilSupplyAndMaterial(Long workInstructionId) {
        return Mono.fromCallable(() -> coilSupplyService.updateFinishCount(workInstructionId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(coilUpdated -> coilUpdated ? updateMaterialStates(workInstructionId) : Mono.error(new RuntimeException("코일 보급 상태 업데이트 실패")))
                .doOnSuccess(v -> log.info("코일 보급 상태 업데이트 완료. 작업 지시서 ID: {}", workInstructionId))
                .doOnError(e -> log.error("코일 보급 상태 업데이트 실패. 작업 지시서 ID: {}", workInstructionId));
    }

    private Mono<Void> updateMaterialStates(Long materialId) {
        return Mono.zip(
                        Mono.fromCallable(() -> materialUpdateService.updateMaterialProgress(materialId, MaterialProgress.H)),
                        Mono.fromCallable(() -> materialUpdateService.reduceThickAndWidth(materialId)),
                        Mono.fromCallable(() -> materialUpdateService.updateProcess(materialId)),
                        Mono.fromCallable(() -> materialUpdateService.updateYard(materialId))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tuple -> {
                    boolean allUpdated = tuple.getT1() && tuple.getT2() && tuple.getT3() && tuple.getT4();
                    return allUpdated ? Mono.empty() : Mono.error(new RuntimeException("재료 상태 업데이트 실패"));
                })
                .doOnSuccess(v -> log.info("재료 상태 업데이트 완료. Material ID: {}", materialId))
                .doOnError(e -> log.error("재료 상태 업데이트 중 하나 이상의 단계에서 실패. Material ID: {}", materialId))
                .then();
    }
}
