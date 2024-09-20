package com.postco.operation.service.impl;

import com.postco.core.dto.ScheduleResultDTO;
import com.postco.operation.service.WorkInstructionService;
import com.postco.operation.service.client.ScheduleServiceClient;
import com.postco.operation.service.redis.OperationRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkInstructionServiceImpl implements WorkInstructionService {
    private final OperationRedisQueryService redisQueryService;
    private final ScheduleServiceClient serviceClient;

    private static final Duration INITIAL_DELAY = Duration.ofSeconds(1);
    private static final Duration MAX_DELAY = Duration.ofSeconds(10);
    private static final int MAX_RETRIES = 3;

    // 카프카 수신
    @KafkaListener(topics = "schedule-confirm-data", groupId = "operation")
    public void handleScheduleResultMessage(String message) {
        log.info("Received Kafka message: {}", message);
        getConfirmedScheduleResults().subscribe(
                results -> log.info("Successfully processed Kafka message and retrieved {} results", results.size()),
                error -> log.error("Error processing Kafka message", error)
        );
    }

    // 1. 레디스로 데이터 요청
    @Override
    public Mono<List<ScheduleResultDTO.View>> getConfirmedScheduleResults() {
        return redisQueryService.fetchAllConfirmSchedules()
                .retryWhen(Retry.backoff(MAX_RETRIES, INITIAL_DELAY)
                        .maxBackoff(MAX_DELAY)
                        .doBeforeRetry(retrySignal ->
                                log.info("Redis 조회 재시도. 시도 횟수: {}", retrySignal.totalRetries() + 1)))
                .doOnNext(cachedResults ->
                        log.info("[Redis {}] {} 개의 확정된 스케줄 결과를 조회했습니다.",
                                cachedResults.isEmpty() ? "실패" : "성공",
                                cachedResults.size()))
                .switchIfEmpty(fetchFromOriginalApi())
                .onErrorResume(error -> {
                    log.error("Redis 조회 최종 실패, API 호출로 대체합니다", error);
                    return fetchFromOriginalApi();
                });
    }

    // 2. 레디스에 저장된 것이 없으면 원본 API 로 요청
    @Override
    public Mono<List<ScheduleResultDTO.View>> fetchFromOriginalApi() {
        return serviceClient.getConfirmResultsFromOrigin()
                .doOnNext(results -> log.info("API 결과 조회 성공. 결과 개수 {} :", results.size()))
                .onErrorResume(error -> {
                    log.error("API 조회 에러 발생", error);
                    return Mono.empty();
                });
    }
}
