package com.postco.operation.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.CoilSupplyDTO;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.operation.domain.entity.CoilSupply;
import com.postco.operation.domain.entity.MaterialProgress;
import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.repository.CoilSupplyRepository;
import com.postco.operation.domain.repository.MaterialRepository;
import com.postco.operation.domain.repository.WorkInstructionRepository;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import com.postco.operation.presentation.dto.WorkInstructionMapper;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
import com.postco.operation.service.WorkInstructionService;
import com.postco.operation.service.client.ScheduleServiceClient;
import com.postco.operation.service.redis.OperationRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkInstructionServiceImpl implements WorkInstructionService {
    private final OperationRedisQueryService redisQueryService;
    private final ScheduleServiceClient serviceClient;
    private final WorkInstructionRepository workInstructionRepository;
    private final MaterialUpdateServiceImpl materialUpdateService;
    private final MaterialRepository materialRepository;
    private final TransactionTemplate transactionTemplate;
    private final CoilSupplyRepository coilSupplyRepository;
    private final ObjectMapper objectMapper;

    private static final Duration INITIAL_DELAY = Duration.ofSeconds(5);
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    private static final int MAX_RETRIES = 3;

    private final AsyncTaskExecutor taskExecutor;

    @KafkaListener(topics = "schedule-confirm-data", groupId = "operation")
    @Transactional
    public void handleScheduleResultMessage(String message) {
        log.info("[Kafka 수신] 카프카 메시지 수신 성공: {}", message);
        taskExecutor.execute(() -> processMessage(message));
    }

    private void processMessage(String message) {
        try {
            ScheduleResultDTO.View scheduleResult = objectMapper.readValue(message, ScheduleResultDTO.View.class);
            processScheduleResults(List.of(scheduleResult))
                    .subscribe(
                            success -> log.info("[메시지 처리 성공] 스케줄 결과 작업대상재 매핑 저장 완료: {}", success),
                            error -> log.error("[메시지 처리 실패] 스케줄 결과 처리 중 오류 발생", error)
                    );
        } catch (JsonProcessingException e) {
            log.error("[메시지 처리 실패] JSON 파싱 오류", e);
        }
    }

    private Mono<Boolean> processScheduleResults(List<ScheduleResultDTO.View> scheduleResults) {
        List<WorkInstructionDTO.Create> dtoList = mapScheduleResultsToWorkInstructions(scheduleResults);
        return saveWorkInstructions(dtoList);
    }

    // 1. 레디스로 데이터 요청
    @Override
    public Mono<List<ScheduleResultDTO.View>> getConfirmedScheduleResults() {
        return Mono.delay(INITIAL_DELAY)
                .then(fetchFromRedis())
                .retryWhen(Retry.fixedDelay(MAX_RETRIES, RETRY_DELAY)
                        .filter(error -> error instanceof IllegalStateException)
                        .doBeforeRetry(retrySignal ->
                                log.info("Redis 조회 재시도. 시도 횟수: {}", retrySignal.totalRetries() + 1)))
                .onErrorResume(error -> {
                    log.error("Redis 조회 최종 실패, API 호출로 대체합니다", error);
                    return fetchFromOriginalApi();
                });
    }

    private Mono<List<ScheduleResultDTO.View>> fetchFromRedis() {
        return redisQueryService.fetchAllConfirmSchedules()
                .flatMap(cachedResults -> {
                    if (cachedResults.isEmpty()) {
                        log.info("Redis 에서 데이터를 찾을 수 없습니다. 재시도합니다.");
                        return Mono.error(new IllegalStateException("Redis 데이터 없음"));
                    } else {
                        log.info("[Redis 성공] {} 개의 확정된 스케줄 결과를 조회했습니다.", cachedResults.size());
                        return Mono.just(cachedResults);
                    }
                });
    }

    // 2. 레디스에 저장된 것이 없으면 원본 API 로 요청
    @Override
    public Mono<List<ScheduleResultDTO.View>> fetchFromOriginalApi() {
        return serviceClient.getConfirmResultsFromOrigin()
                .doOnNext(results -> log.info("API 결과 조회 성공. 결과 개수: {}", results.size()))
                .onErrorResume(error -> {
                    log.error("API 조회 에러 발생", error);
                    return Mono.empty();
                });
    }

    @Override
    public List<WorkInstructionDTO.Create> mapScheduleResultsToWorkInstructions(List<ScheduleResultDTO.View> scheduleResults) {
        return scheduleResults.stream()
                .map(result -> WorkInstructionMapper.mapToWorkInstructionDTO(result, generateWorkNo(result.getProcess(), result.getRollUnit())))
                .collect(Collectors.toList());
    }


    @Override
    public Mono<Boolean> saveWorkInstructions(List<WorkInstructionDTO.Create> workInstructions) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    List<WorkInstruction> savedInstructions = workInstructions.stream()
                            .map(dto -> WorkInstructionMapper.mapToEntity(dto, materialRepository))
                            .collect(Collectors.collectingAndThen(Collectors.toList(), workInstructionRepository::saveAll));

                    // 코일 보급 현황 저장
                    savedInstructions.forEach(instruction -> {
                        if (coilSupplyRepository.findByWorkInstruction(instruction).isEmpty()) {
                            CoilSupply coilSupply = new CoilSupply();
                            coilSupply.setWorkInstruction(instruction);
                            coilSupply.setTotalCoils(instruction.getTotalQuantity());
                            coilSupply.setSuppliedCoils(0);
                            coilSupply.setTotalProgressed(0);
                            coilSupply.setTotalRejects(0);
                            coilSupplyRepository.save(coilSupply);
                        }
                    });

                    // 재료 상태 E 로 변경
                    savedInstructions.stream()
                            .flatMap(instruction -> instruction.getItems().stream())
                            .map(WorkInstructionItem::getMaterial)
                            .filter(Objects::nonNull)
                            .forEach(material -> {
                                materialUpdateService.updateMaterialProgress(material.getId(), MaterialProgress.E);
                            });
                    return true;
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }


    // 랜덤 no 생성 함수 -> 작업지시서의 no 에 넣으면 됨.
    private String generateWorkNo(String processCode, String rollUnit) {
        Long lastId = workInstructionRepository.findLastSavedId();
        long nextId = (lastId != null) ? lastId + 1 : 1;
        String sequence = String.format("%03d", nextId);
        String randomChars = new SecureRandom().ints(2, 0, 26)
                .mapToObj(i -> String.valueOf((char) ('A' + i)))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        String truncatedProcessCode = processCode.length() >= 2 ? processCode.substring(0, 2) : processCode;
        return String.format("W%s%s%s%s", truncatedProcessCode, sequence, randomChars, rollUnit);
    }


    // ============= 조회 부분 (cqrs 패턴에 따라 분리해야함.. 나중에 리팩토링.. )
    @Override
    public Mono<List<WorkInstructionDTO.View>> getWorkInstructions(String process, String rollUnit) {
        return Mono.fromCallable(() -> {
            log.info("작업 지시서 조회 서비스 시작. 공정: {}, 롤 단위: {}", process, rollUnit);
            List<WorkInstruction> workInstructions = workInstructionRepository.findByProcessAndRollUnit(process, rollUnit);
            log.info("작업지시문 : {}", workInstructions.get(0).getItems().get(0).getMaterial());
            List<WorkInstructionDTO.View> dtos = workInstructions.stream()
                    .map(WorkInstructionMapper::mapToDto)
                    .collect(Collectors.toList());
            log.info("작업 지시서 조회 완료. 조회된 작업 지시서 수: {}", dtos.size());
            return dtos;
        }).subscribeOn(Schedulers.boundedElastic());  // 블로킹 작업을 별도의 스레드 풀에서 실행
    }

    @Override
    public Mono<List<WorkInstructionDTO.View>> getUncompletedWorkInstructions(String process) {
        return Mono.fromCallable(() -> {
            log.info("작업 지시서 조회 서비스 시작. 공정: {}, 롤 단위: {}", process);
            List<WorkInstruction> workInstructions = workInstructionRepository.findUncompletedWithItems(process);
            log.info("작업지시문 : {}", workInstructions.get(0));
            List<WorkInstructionDTO.View> dtos = workInstructions.stream()
                    .map(WorkInstructionMapper::mapToDto)
                    .collect(Collectors.toList());
            log.info("작업 지시서 조회 완료. 조회된 작업 지시서 수: {}", dtos.size());
            log.info("매핑된 작업지시문 : {}", dtos);
            return dtos;
        }).subscribeOn(Schedulers.boundedElastic());  // 블로킹 작업을 별도의 스레드 풀에서 실행
    }
    @Override
    public Mono<List<WorkInstructionDTO.SimulationView>> getUncompletedWorkInstructionsForSimulation() {
        return Mono.fromCallable(() -> {
            log.info("시뮬레이션 작업 지시서 조회 서비스 시작. 공정: {}, 롤 단위: {}");
            List<WorkInstruction> workInstructions = workInstructionRepository.findUncompletedWithItemsForSimulation();
            log.info("작업지시문 : {}", workInstructions.get(0));
            List<WorkInstructionDTO.SimulationView> dtos = workInstructions.stream()
                    .map(WorkInstructionMapper::mapToSimulationView)
                    .collect(Collectors.toList());
            log.info("작업 지시서 조회 완료. 조회된 작업 지시서 수: {}", dtos.size());
            log.info("매핑된 작업지시문 : {}", dtos);
            return dtos;
        }).subscribeOn(Schedulers.boundedElastic());  // 블로킹 작업을 별도의 스레드 풀에서 실행
    }

    /*
     * 추가 Sohyun Ahn 240930,
     */
    @Transactional
    @Override
    public Mono<List<ClientDTO>> getUncompletedWorkInstructionsBeforeWebSocket(String process) {
        return Mono.fromCallable(() -> {
                    // 작업 지시서 조회
                    List<WorkInstruction> workInstructions = workInstructionRepository.findUncompletedWithItems(process);
                    log.info("조회된 작업 지시서 수: {}", workInstructions.size());

                    return clientDTOMapper(workInstructions);
                }).subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("작업 지시서 조회 완료. 반환된 ClientDTO 수: {}", result.size()))
                .doOnError(error -> log.error("작업 지시서 조회 중 오류 발생", error));
    }

    @Override
    public Mono<List<WorkInstructionDTO.View>> getCompletedWorkInstructions(String process, String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startDateTime = LocalDate.parse(startDate, formatter).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);
        log.info("시간 변환 start : {} , end : {}", startDateTime, endDateTime);
        return Mono.fromCallable(() -> {
            log.info("작업 지시서 조회 서비스 시작. 공정: {}, 롤 단위: {}", process);
            List<WorkInstruction> workInstructions = workInstructionRepository.findCompletedWithItems(process, startDateTime, endDateTime);
            log.info("repository 를 통한 조회 : {}", workInstructions);
            List<WorkInstructionDTO.View> dtos = workInstructions.stream()
                    .map(WorkInstructionMapper::mapToDto)
                    .collect(Collectors.toList());
            log.info("작업 지시서 조회 완료. 조회된 작업 지시서 수: {}", dtos.size());
            return dtos;
        }).subscribeOn(Schedulers.boundedElastic());  // 블로킹 작업을 별도의 스레드 풀에서 실행
    }

    @Override
    @Transactional
    public Mono<List<ClientDTO>> getInProgressWorkInstructions() {
        return Mono.fromCallable(() -> {
                    log.info("웹소켓 작업 지시서 조회 서비스 시작. 공정: {}, 롤 단위: {}");
                    List<WorkInstruction> workInstructions = workInstructionRepository.findInProgressWorkInstructions();
                    log.info("조회된 웹소켓용 작업 지시서 수: {}", workInstructions.size());
                    return clientDTOMapper(workInstructions);
                }).subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("작업 지시서 조회 완료. 반환된 ClientDTO 수: {}", result.size()))
                .doOnError(error -> log.error("작업 지시서 조회 중 오류 발생", error));
    }

    public List<ClientDTO> clientDTOMapper(List<WorkInstruction> workInstructions) {
        return workInstructions.stream()
                .map(workInstruction -> {
                    // WorkInstruction을 ClientDTO.Message로 매핑
                    WorkInstructionDTO.Message workInstructionMessage = WorkInstructionMapper.mapToMessageDto(workInstruction);

                    log.info("작업 지시서 매핑 성공: {}", workInstructionMessage);

                    // CoilSupply 가져오기
                    Optional<CoilSupply> optionalCoilSupply = coilSupplyRepository.findByWorkInstructionIdWithWorkInstruction(workInstruction.getId());
                    CoilSupplyDTO.Message coilSupplyMessage = optionalCoilSupply.map(coilSupply -> CoilSupplyDTO.Message.builder()
                                    .coilSupplyId(coilSupply.getId())
                                    .workInstructionId(coilSupply.getWorkInstruction().getId())
                                    .workStatus(String.valueOf(coilSupply.getWorkInstruction().getWorkStatus()))
                                    .totalCoils(coilSupply.getTotalCoils())
                                    .suppliedCoils(coilSupply.getSuppliedCoils())
                                    .totalProgressed(coilSupply.getTotalProgressed())
                                    .totalRejects(coilSupply.getTotalRejects())
                                    .build())
                            .orElse(null);

                    log.info("현재 진행 상황 통계 매핑 성공: {}", coilSupplyMessage);

                    // CoilTypeCode를 그룹화하여 카운트 (items에서 coilTypeCode 추출)
                    Map<String, Integer> countCoilTypeCode = workInstructionMessage.getItems().stream()
                            .collect(Collectors.groupingBy(
                                    WorkInstructionItemDTO.Message::getCoilTypeCode, // 그룹화 기준
                                    Collectors.summingInt(item -> 1) // 각 그룹의 개수 카운트
                            ));

                    log.info("coilTypeCode 카운트 결과: {}", countCoilTypeCode);

                    // ClientDTO 생성
                    return ClientDTO.builder()
                            .workInstructions(workInstructionMessage)
                            .coilSupply(coilSupplyMessage)
                            .countCoilTypeCode(countCoilTypeCode)
                            .build();

                })
                .collect(Collectors.toList()); // 리스트로 반환
    }
}