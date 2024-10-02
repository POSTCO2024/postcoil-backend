package com.postco.operation.service.impl.work;

import com.postco.operation.domain.entity.*;
import com.postco.operation.domain.repository.CoilSupplyRepository;
import com.postco.operation.domain.repository.WorkInstructionRepository;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
import com.postco.operation.presentation.dto.websocket.WebSocketMessageType;
import com.postco.operation.service.CoilSupplyService;
import com.postco.operation.service.MaterialUpdateService;
import com.postco.operation.service.WorkItemService;
import com.postco.operation.service.impl.SupplyQueueManager;
import com.postco.operation.service.impl.WorkInstructionServiceImpl;
import com.postco.operation.service.util.WorkSimulationUtil;
import com.postco.websocket.service.CoilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoilWorkCommandService {
    private final WorkInstructionRepository workInstructionRepository;
    private final WorkItemService workItemService;
    private final CoilSupplyRepository coilSupplyRepository;
    private final WorkItemRepository workItemRepository;
    private final CoilSupplyService coilSupplyService;
    private final MaterialUpdateService materialUpdateService;
    private final TransactionTemplate transactionTemplate;
    private final SupplyQueueManager supplyQueueManager;
    private final ClientDashboardService clientDashboardService;
    private final WorkInstructionServiceImpl workInstructionService;
    private final CoilService coilService;

    // 작업 지시서에서 보급 요청 시 관련 작업 수행
    // 1) 코일 보급 처리 -> 업데이트
    // 2) 코일 작업 시작 및 종료
    public Mono<Boolean> requestSupply(Long workInstructionId, int supplyCount) {
        // 웹소켓으로 정보 전달
        Mono<List<ClientDTO>> socketData = workInstructionService.getInProgressWorkInstructions();
        socketData.subscribe(data -> {
            log.info("보낼 웹소켓 정보 보급 클릭시 : {}", data); // 실제 데이터를 로깅
            coilService.directMessageToClient("/topic/work-instruction", data); // 데이터를 전송
        });
        return Mono.fromCallable(() ->
                        transactionTemplate.execute(status -> {
                            WorkInstruction workInstruction = workInstructionRepository.findByIdWithItems(workInstructionId)
                                    .orElseThrow(() -> new IllegalArgumentException("Invalid workInstructionId"));

                            CoilSupply coilSupply = coilSupplyRepository.findByWorkInstruction(workInstruction)
                                    .orElseThrow(() -> new IllegalArgumentException("No CoilSupply found for the work instruction"));

                            String equipmentCode = workInstruction.getProcess(); // 공정 코드를 설비 코드로 사용
                            log.info("작업 지시서 ID: {}, 설비 코드: {}, 보급 요청 수량: {}", workInstructionId, equipmentCode, supplyCount);

                            List<WorkInstructionItem> itemsToQueue = prepareItemsForQueue(workInstruction, supplyCount);

                            return Tuples.of(itemsToQueue, coilSupply, equipmentCode);
                        })
                ).subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    List<WorkInstructionItem> itemsToQueue = result.getT1();
                    CoilSupply coilSupply = result.getT2();
                    String equipmentCode = result.getT3();
                    return supplyQueueManager.addItemsToQueue(equipmentCode, workInstructionId, itemsToQueue)
                            .doOnSuccess(success -> {
                                supplyQueueManager.processSupplyInBackground(equipmentCode, coilSupply, itemsToQueue.size(), this::startWorkOnItem)
                                        .doOnError(error -> log.error("백그라운드 처리 중 오류 발생", error))
                                        .doOnSuccess(processed -> log.info("백그라운드 처리 완료"))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .subscribe();
                            })
                            .thenReturn(true);
                })
                .onErrorResume(this::handleSupplyRequestError);
    }

    // 작업 아이템을 sequence 순서대로 큐에 추가
    private List<WorkInstructionItem> prepareItemsForQueue(WorkInstruction workInstruction, int supplyCount) {
        List<WorkInstructionItem> itemsToQueue = workInstruction.getItems().stream()
                .filter(item -> item.getWorkItemStatus() == WorkStatus.PENDING)
                .sorted(Comparator.comparingInt(WorkInstructionItem::getSequence))
                .limit(supplyCount)
                .collect(Collectors.toList());

        if (itemsToQueue.isEmpty()) {
            throw new RuntimeException("더 이상 보급할 작업 아이템이 없습니다.");
        }

        return itemsToQueue;
    }

    private Mono<Boolean> handleSupplyRequestError(Throwable e) {
        log.error("보급 요청 중 오류 발생", e);
        return Mono.just(false);
    }

    // 작업 아이템 작업 시작 -> 업데이트
    public Mono<Long> startWorkOnItem(Long itemId) {
        return Mono.fromCallable(() -> workItemRepository.findById(itemId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalItem -> optionalItem
                        .map(item -> workItemService.startWorkItem(item.getId())
                                .flatMap(updatedItem -> {
                                    if (updatedItem != null) {
                                        // 관제로 웹소켓 통해 정보 전달
                                        clientDashboardService.sendAnalyticsDashboardStartData(WebSocketMessageType.WORK_STARTED);
                                        // 웹소켓으로 정보 전달
                                        Mono<List<ClientDTO>> socketData = workInstructionService.getInProgressWorkInstructions();
                                        socketData.subscribe(data -> {
                                            log.info("보낼 웹소켓 정보 작업시작시 : {}", data); // 실제 데이터를 로깅
                                            coilService.directMessageToClient("/topic/work-instruction", data); // 데이터를 전송
                                        });
                                        return scheduleWorkCompletion(updatedItem)
                                                .thenReturn(itemId);
                                    }
                                    log.error("작업 시작 실패, 아이템 ID: {}", itemId);
                                    return Mono.empty();
                                }))
                        .orElseGet(() -> {
                            log.error("작업 시작 실패, 아이템을 찾을 수 없음. 아이템 ID: {}", itemId);
                            return Mono.empty();
                        }));
    }

    private Mono<Void> scheduleWorkCompletion(WorkInstructionItem item) {
        long expectedDurationMinutes = item.getExpectedItemDuration();
        return completeWorkAfterDuration(item.getId(), item.getMaterial().getId(), expectedDurationMinutes)
                .doOnSubscribe(s -> log.info("[작업 완료] 완료 프로세스 start... - 아이템 ID: {}", item.getId()))
                .doOnError(error -> log.error("[작업 완료] 완료 프로세스 에러 - 아이템 ID: {}, 에러: {}", item.getId(), error.getMessage()));
    }

    // step 3.
    // 일정 시간 이후 작업 종료, 1) 작업 아이템 업데이트 -> finishWorkItem
    // 2) 코일 보급 업데이트 -> updateFinishCount
    // 3) 재료 상태 업데이트 -> updateMaterialProgress, reduceThickAndWidth , updateProcess, updateYard
    public Mono<Void> completeWorkAfterDuration(Long itemId, Long materialId, long expectedDurationMinutes) {
        long presentationDuration = WorkSimulationUtil.convertToPresentationDuration((int) expectedDurationMinutes);
        log.info("작업 완료 예정 - 아이템 ID: {}, 예상 소요 시간: {} ms", itemId, presentationDuration);
        return Mono.delay(Duration.ofMillis(presentationDuration))
                .doOnNext(v -> log.info("작업 완료 지연 종료 - 아이템 ID: {}", itemId))
                .then(finishWorkUpdates(itemId, materialId))
                .doOnSuccess(v -> {
                    log.info("작업 완료 처리 성공 - 아이템 ID: {}", itemId);
                    // 작업 완료 시 관제로 데이터 전송
                    clientDashboardService.sendAnalyticsDashboardEndData(WebSocketMessageType.WORK_COMPLETED);

                    // 웹소켓으로 정보 전달
                    Mono<List<ClientDTO>> socketData = workInstructionService.getInProgressWorkInstructions();
                    socketData.subscribe(data -> {
                        log.info("보낼 웹소켓 정보 작업완료 시 : {}", data); // 실제 데이터를 로깅
                        coilService.directMessageToClient("/topic/work-instruction", data); // 데이터를 전송
                    });
                })
                .doOnError(error -> log.error("작업 완료 처리 실패 - 아이템 ID: {}, 에러: {}", itemId, error.getMessage()));
    }

    private Mono<Void> finishWorkUpdates(Long itemId, Long materialId) {
        return workItemService.finishWorkItem(itemId)
                .doOnSuccess(success -> log.info("작업 아이템 종료 업데이트 결과 - 아이템 ID: {}, 성공: {}", itemId, success))
                .flatMap(success -> {
                    if (!success) {
                        return Mono.error(new RuntimeException("작업 아이템 종료 업데이트 실패"));
                    }
                    return updateCoilSupplyAndMaterial(itemId, materialId);
                })
                .doOnSuccess(v -> log.info("작업 아이템 업데이트 완료. ID: {}", itemId))
                .doOnError(e -> log.error("작업 아이템 종료 업데이트 실패. ID: {}", itemId, e));
    }

    private Mono<Void> updateCoilSupplyAndMaterial(Long itemId, Long materialId) {
        return Mono.fromCallable(() -> {
                    WorkInstructionItem item = workItemRepository.findById(itemId)
                            .orElseThrow(() -> new IllegalArgumentException("작업 아이템을 찾을 수 없습니다."));
                    return coilSupplyService.updateFinishCount(item.getWorkInstruction().getId());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(coilUpdated -> coilUpdated ? updateMaterialStates(materialId) : Mono.error(new RuntimeException("코일 보급 상태 업데이트 실패")))
                .doOnSuccess(v -> log.info("코일 보급 상태 업데이트 완료. 작업 아이템 ID: {}", itemId))
                .doOnError(e -> log.error("코일 보급 상태 업데이트 실패. 작업 아이템 ID: {}", itemId));
    }

    // 재료 아이디를 넘겨야함
    // 작업지시서 아이템에 material_id 존재
    private Mono<Void> updateMaterialStates(Long materialId) {
        return Mono.zip(
                        Mono.fromCallable(() -> materialUpdateService.updateMaterialProgress(materialId, MaterialProgress.H)),
                        Mono.fromCallable(() -> materialUpdateService.reduceThickAndWidth(materialId)),
                        Mono.fromCallable(() -> materialUpdateService.updateProcess(materialId)),
                        Mono.fromCallable(() -> materialUpdateService.updateYard(materialId, "B"))
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
