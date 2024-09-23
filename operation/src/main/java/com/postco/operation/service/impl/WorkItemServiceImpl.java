package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.service.WorkItemService;
import com.postco.operation.service.util.WorkSimulationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemServiceImpl implements WorkItemService {
    private final WorkItemRepository workItemRepository;

    @Override
    @Transactional
    public boolean rejectWorkItem(Long itemId) {
        try {
            WorkInstructionItem item = workItemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("작업 아이템을 찾을 수 없습니다."));

            item.updateReject();
            workItemRepository.save(item);

            log.info("작업 아이템 리젝트 처리 완료. ID: {}", itemId);
            return true;
        } catch (Exception e) {
            log.error("작업 아이템 리젝트 처리 중 오류 발생. ID: {}", itemId, e);
            return false;
        }
    }

    @Override
    public Mono<Boolean> startWorkItem(Long itemId) {
        return Mono.defer(() -> Mono.fromCallable(() -> workItemRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("작업 아이템을 찾을 수 없습니다.")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(item -> {
                    item.startWork();
                    return Mono.fromCallable(() -> workItemRepository.save(item))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .flatMap(this::scheduleWorkCompletion)
                .thenReturn(true)
                .doOnSuccess(result -> log.info("ID: {} 코일 작업 시작", itemId))
                .onErrorResume(e -> {
                    log.error("작업 시작 처리 중 오류 발생. ID: {}", itemId, e);
                    return Mono.just(false);
                }));
    }

    private Mono<WorkInstructionItem> scheduleWorkCompletion(WorkInstructionItem item) {
        long presentationDuration = WorkSimulationUtil.convertToPresentationDuration(Math.toIntExact(item.getExpectedItemDuration()));
        return WorkSimulationUtil.simulateWorkCompletion(presentationDuration)
                .then(finishWorkItem(item.getId()))
                .thenReturn(item);
    }

    @Override
    public Mono<Boolean> finishWorkItem(Long itemId) {
        return Mono.defer(() -> Mono.fromCallable(() -> workItemRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("작업 아이템을 찾을 수 없습니다.")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(item -> {
                    item.finishWork();
                    return Mono.fromCallable(() -> workItemRepository.save(item))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .thenReturn(true)
                .doOnSuccess(result -> log.info("ID: {} 코일 작업 종료", itemId))
                .onErrorResume(e -> {
                    log.error("작업 아이템 종료 처리 중 오류 발생. ID: {}", itemId, e);
                    return Mono.just(false);
                }));
    }


}
