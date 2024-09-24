package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.repository.WorkInstructionRepository;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.service.WorkItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemServiceImpl implements WorkItemService {
    private final WorkItemRepository workItemRepository;
    private final WorkInstructionRepository workInstructionRepository;
    private final TransactionTemplate transactionTemplate;

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
    public Mono<WorkInstructionItem> startWorkItem(Long itemId) {
        return Mono.fromCallable(() ->
                        transactionTemplate.execute(status -> {
                            WorkInstructionItem item = workItemRepository.findByIdWithWorkInstruction(itemId)
                                    .orElseThrow(() -> new IllegalArgumentException("작업 아이템을 찾을 수 없습니다."));
                            WorkInstruction workInstruction = item.getWorkInstruction();

                            item.startWork();
                            log.info("작업지시 아이템 상태 {} , 시작 시간 : {} : ", item.getWorkItemStatus(), item.getStartTime());
                            workInstruction.updateStatus();

                            workItemRepository.save(item);
                            workInstructionRepository.save(workInstruction);
                            return item;
                        })
                ).subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("[작업 시작] 아이템 ID: {} 코일 작업 Start ...", itemId))
                .onErrorResume(e -> {
                    log.error("작업 시작 처리 중 오류 발생. ID: {}", itemId, e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Boolean> finishWorkItem(Long itemId) {
        return Mono.fromCallable(() ->
                        transactionTemplate.execute(status -> {
                            WorkInstructionItem item = workItemRepository.findByIdWithWorkInstruction(itemId)
                                    .orElseThrow(() -> new IllegalArgumentException("작업 아이템을 찾을 수 없습니다."));
                            WorkInstruction workInstruction = item.getWorkInstruction();

                            item.finishWork();
                            workInstruction.updateStatus();

                            workItemRepository.save(item);
                            workInstructionRepository.save(workInstruction);

                            return true;
                        })
                ).subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("[작업 종료] 아이템 ID: {} 코일 작업 finish ...", itemId))
                .onErrorResume(e -> {
                    log.error("작업 아이템 종료 처리 중 오류 발생. ID: {}", itemId, e);
                    return Mono.just(false);
                });
    }
}
