package com.postco.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final ScheduleRedisService scheduleRedisService;

    @Override
    public void run(String... args) {
        loadDataFromRedis().block(); // 앱 시작 시, 데이터 로딩 완료될 때 까지 wait..
    }

    private Mono<Void> loadDataFromRedis() {
        return Mono.zip(
                scheduleRedisService.getAllTargetFromRedis(),
                scheduleRedisService.getAllMaterialFromRedis()
        ).doOnNext(tuple -> {
            log.info("[Redis 성공] 작업대상재 총 개수 : {} 로드 완료 ", tuple.getT1().size());
            log.info("[Redis 성공] 재료 총 개수: {} 로드 완료", tuple.getT2().size());
        }).then();
    }
}
