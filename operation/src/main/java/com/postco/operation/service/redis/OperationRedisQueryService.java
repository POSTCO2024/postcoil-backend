package com.postco.operation.service.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.ScheduleMaterialDTO;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.core.redis.cqrs.query.GenericRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OperationRedisQueryService {
    private final GenericRedisQueryService redisQueryService;
    private final ObjectMapper objectMapper;

    public Mono<List<ScheduleResultDTO.View>> fetchAllConfirmSchedules() {
        return redisQueryService.fetchAllBySinglePrefixWithJsonField(
                        "schedule:",
                        ScheduleResultDTO.View.class,
                        "materials",
                        new TypeReference<List<ScheduleMaterialDTO>>() {})
                .collectList()
                .doOnNext(confirm -> log.info("[Redis 성공] 모든 승인된 스케쥴 결과를 Redis 로부터 불러왔습니다. 개수: {}", confirm.size()));
    }

    public Mono<List<ScheduleResultDTO.View>> fetchAllConfirmScheduleTest() {
        return redisQueryService.fetchAllBySinglePrefixWithJsonField(
                        "schedule:",
                        ScheduleResultDTO.View.class,
                        "materials",
                        new TypeReference<List<ScheduleMaterialDTO>>() {})
                .doOnNext(schedule -> {
                    log.debug("Fetched schedule from Redis: {}", schedule);
                    log.debug("Materials in schedule: {}", schedule.getMaterials());
                })
                .collectList()
                .doOnNext(schedules -> {
                    log.info("[Redis 성공] 스케쥴 결과를 Redis 로부터 불러왔습니다. 개수: {}", schedules.size());
                    schedules.forEach(schedule -> {
                        log.debug("Schedule: {}", schedule);
                        log.debug("스케쥴 대상재 : {}", schedule.getMaterials());
                    });
                })
                .doOnError(error -> log.error("Error fetching schedules from Redis", error));
    }

}