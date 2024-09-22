package com.postco.operation.service;

import com.postco.operation.domain.entity.*;
import com.postco.operation.domain.repository.CoilSupplyRepository;
import com.postco.operation.domain.repository.WorkInstructionRepository;
import com.postco.operation.domain.repository.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoilWorkCommandService {

    private static final long DELAY_IN_SECONDS = 2L;  // 설비 보급 완료 시간
    private static final String SUPPLY_QUEUE_KEY = "supplyQueue";  // Redis 큐 키

    private final WorkInstructionRepository workInstructionRepository;
    private final WorkItemRepository workItemRepository;
    private final CoilSupplyRepository coilSupplyRepository;
    private final CoilSupplyService coilSupplyService;
    private final MaterialUpdateService materialUpdateService;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    // 작업 지시서에서 보급 요청 시 순서대로 큐에 넣기
    @Transactional
    public Mono<Boolean> requestSupply(Long workInstructionId, int supplyCount) {
        return Mono.fromCallable(() -> {
                    WorkInstruction workInstruction = workInstructionRepository.findById(workInstructionId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid workInstructionId"));

                    CoilSupply coilSupply = coilSupplyRepository.findByWorkInstruction(workInstruction)
                            .orElseThrow(() -> new IllegalArgumentException("No CoilSupply found for the work instruction"));

                    log.info("작업 지시서 ID: {}, 보급 요청 수량: {}", workInstructionId, supplyCount);

                    // 1. 큐에 작업아이템 추가
                    addItemsToQueue(workInstruction, supplyCount);

                    // 2. 일정 시간 후 보급 완료 처리
                    completeSupplyAfterDelay(coilSupply, supplyCount, DELAY_IN_SECONDS);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // 작업 아이템을 sequence 순서대로 큐에 추가
    private void addItemsToQueue(WorkInstruction workInstruction, int supplyCount) {
        // 작업 아이템을 sequence 순서로 정렬
        List<WorkInstructionItem> sortedItems = workInstruction.getItems().stream()
                .filter(item -> item.getWorkItemStatus() == WorkStatus.PENDING)
                .sorted(Comparator.comparingInt(WorkInstructionItem::getSequence))  // sequence 기준 정렬
                .collect(Collectors.toList());

        int addedCount = 0;
        for (WorkInstructionItem item : sortedItems) {
            if (addedCount >= supplyCount) break;
            redisTemplate.opsForList().leftPush(SUPPLY_QUEUE_KEY, item.getId())
                    .doOnSuccess(result -> log.info("[보급 요청 성공] 큐에 작업 아이템 ID : {} 추가됨", item.getId()))
                    .doOnError(error -> log.error("[보급 요청 실패] 큐 추가 중 오류 발생: {}", error.getMessage()))
                    .subscribe();

            addedCount++;
        }

        log.info("[보급 요청 완료] 총 {}개의 작업 아이템이 큐에 추가됨. 작업 지시서 ID: {}", addedCount, workInstruction.getId());
    }

    // 일정 시간 후 보급 완료 처리 및 큐에서 아이템 작업 시작
    private void completeSupplyAfterDelay(CoilSupply coilSupply, int suppliedCount, long delayInSeconds) {
        Mono.delay(Duration.ofSeconds(delayInSeconds))
                .doOnNext(tick -> coilSupplyService.updateCoilSupply(coilSupply.getWorkInstruction().getId(), suppliedCount))
                .doOnNext(tick -> processQueuedItems(suppliedCount))
                .doOnSuccess(result -> log.info("보급 완료된 코일 수: {}", coilSupply.getSuppliedCoils()))
                .doOnError(error -> log.error("보급 처리 중 오류 발생: {}", error.getMessage()))
                .subscribe();
    }

    // 큐에서 작업아이템 처리
    private void processQueuedItems(int count) {
        IntStream.range(0, count)
                .forEach(i -> redisTemplate.opsForList().rightPop(SUPPLY_QUEUE_KEY)
                        .map(itemId -> (Long) itemId)
                        .doOnNext(id -> log.info("큐에서 작업 아이템 ID 처리: {}", id))
                        .doOnNext(this::startWorkOnItem)  // 아이템 작업 시작
                        .switchIfEmpty(Mono.fromRunnable(() -> log.info("더 이상 처리할 아이템이 없습니다.")))
                        .block());
    }

    // step 2.
    // 보급 완료된 아이템 작업 시작 및 재료 상태 업데이트
    @Transactional
    public void startWorkOnItem(Long itemId) {
        WorkInstructionItem item = workItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid WorkInstructionItem ID"));

        // 작업 시작
        item.startWork();
        workItemRepository.save(item);

        // 재료 상태를 'E'로 업데이트
        materialUpdateService.updateMaterialProgress(item.getMaterial().getId(), MaterialProgress.E);

        log.info("작업 시작됨 - 아이템 ID: {}, 재료 ID: {}, 진행 상태: E", item.getId(), item.getMaterial().getId());
    }
}
