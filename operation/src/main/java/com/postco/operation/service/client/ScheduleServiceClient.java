package com.postco.operation.service.client;

import com.postco.core.dto.ScheduleResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 캐시에 데이터가 없을 때,
 * 원본 데이터를 요청하는 클라이언트 서비스 입니다.
 * 스케쥴 결과를 요청합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleServiceClient {
    private final WebClient scheduleServiceClient;

    public Mono<List<ScheduleResultDTO.View>> getConfirmResultsFromOrigin() {
        return scheduleServiceClient.get()
                .uri("lb://schedule/api/v1/schedule/confirmed-results")
                .retrieve()
                .bodyToFlux(ScheduleResultDTO.View.class)
                .collectList()
                .doOnNext(results -> log.info("[API 호출 성공] API 로부터 승인된 스케줄 결과를 {} 개 가져왔습니다.", results.size()))
                .doOnError(error -> log.error("[API 호출 에러] API 호출 오류 발생", error));
    }
}
