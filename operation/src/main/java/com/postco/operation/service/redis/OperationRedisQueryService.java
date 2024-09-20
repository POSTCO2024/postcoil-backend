package com.postco.operation.service.redis;

import com.postco.core.dto.ScheduleResultDTO;
import com.postco.core.dto.TargetMaterialDTO;
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

    public Mono<List<ScheduleResultDTO.View>> fetchAllConfirmSchedules() {
        return redisQueryService.fetchAllBySinglePrefix("schedule:", ScheduleResultDTO.View.class)
                .collectList()
                .doOnNext(confirm -> log.info("[Redis 성공] 모든 승인된 스케쥴 결과를 Redis 로부터 불러왔습니다. 개수: {}", confirm.size()));
    }
}
