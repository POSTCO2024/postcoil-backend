package com.postco.operation.service.impl.work;

import com.postco.operation.domain.entity.MaterialProgress;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.entity.WorkStatus;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.service.MaterialUpdateService;
import com.postco.operation.service.util.WorkSimulationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoilDeliveryService {
    private final WorkItemRepository workItemRepository;
    private final MaterialUpdateService materialUpdateService;
    private final TransactionTemplate transactionTemplate;

//    @PostConstruct
//    public void init() {
//        // 서비스 시작 시 즉시 실행
//        processCompletedWorkItems()
//                .doOnSuccess(v -> log.info("서비스 시작 시 완료된 작업 아이템 처리 완료"))
//                .doOnError(e -> log.error("서비스 시작 시 완료된 작업 아이템 처리 중 오류 발생", e))
//                .subscribe();
//    }

    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void scheduleCompletedWorkItemsProcessing() {
        processCompletedWorkItems()
                .doOnSuccess(v -> log.info("완료된 작업 아이템 처리 완료"))
                .doOnError(e -> log.error("완료된 작업 아이템 처리 중 오류 발생", e))
                .subscribe();
    }

    public Mono<Void> processCompletedWorkItems() {
        return Mono.fromCallable(this::findCompletedWorkItems)
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::processWorkItem)
                .then();
    }

    private List<WorkInstructionItem> findCompletedWorkItems() {
        return transactionTemplate.execute(status -> workItemRepository.findAllByWorkItemStatus(WorkStatus.COMPLETED)
                .orElseGet(() -> {
                    log.info("완료된 작업 아이템이 없습니다.");
                    return Collections.emptyList();
                }));
    }

    private Mono<Void> processWorkItem(WorkInstructionItem item) {
        return Mono.just(item)
                .flatMap(this::startDeliverySimulation)
                .flatMap(this::updateMaterialAfterDelivery)
                .onErrorResume(e -> {
                    log.error("작업 아이템 처리 중 오류 발생. Item ID: {}", item.getId(), e);
                    return Mono.empty();
                });
    }

    private Mono<WorkInstructionItem> startDeliverySimulation(WorkInstructionItem item) {
        return Mono.fromCallable(() -> {
            boolean updated = materialUpdateService.updateMaterialProgress(item.getMaterial().getId(), MaterialProgress.J);
            if (!updated) {
                throw new RuntimeException("재료 진도 업데이트 실패");
            }
            return item;
        }).flatMap(updatedItem ->
                WorkSimulationUtil.simulateDelivery().thenReturn(updatedItem)
        );
    }

    private Mono<Void> updateMaterialAfterDelivery(WorkInstructionItem item) {
        return Mono.fromCallable(() -> {
            Long materialId = item.getMaterial().getId();
            boolean progressUpdated = materialUpdateService.updateMaterialProgress(materialId, MaterialProgress.D);
            boolean yardUpdated = materialUpdateService.updateYard(materialId, "A");
            boolean deliveryUpdated = materialUpdateService.updateAfterDelivery(materialId);

            if (!progressUpdated || !yardUpdated || !deliveryUpdated) {
                throw new RuntimeException("이송 후 재료 상태 업데이트 실패");
            }
            return item;
        }).then();
    }
}
