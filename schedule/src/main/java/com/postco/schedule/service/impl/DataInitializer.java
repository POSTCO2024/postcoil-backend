package com.postco.schedule.service.impl;

import com.postco.core.dto.RedisDataContainer;
import com.postco.core.dto.RefDataContainer;
import com.postco.schedule.service.ScheduleRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final ScheduleRedisService scheduleRedisService;
    private final RegisterServiceImpl registerService;

    @Override
    public void run(String... args) {
        // 비동기로 데이터 로딩 및 스케줄 대상재 등록을 실행
        initializeData();
    }

    private void initializeData() {
        // 작업대상재 및 재료 데이터를 비동기적으로 로드하고 처리
        loadScheduleData()
                .flatMap(this::registerScheduleMaterials)
                .doOnSuccess(ignored -> log.info("[스케줄 대상재 등록 완료]"))
                .doOnError(error -> log.error("[스케줄 대상재 등록 중 오류 발생]", error))
                .subscribe();
    }

    // 스케줄 데이터 및 설비 데이터를 Redis에서 불러오는 메서드
    private Mono<Tuple2<RedisDataContainer, RefDataContainer>> loadScheduleData() {
        return Mono.zip(
                        scheduleRedisService.getScheduleData(),   // 작업대상재 및 재료 데이터
                        scheduleRedisService.getReferenceData()   // 설비 데이터
                )
                .doOnNext(tuple -> {
                    log.info("[Redis 성공] 작업대상재 총 개수: {} 로드 완료", tuple.getT1().getTargetMaterials().size());
                    log.info("[Redis 성공] 재료 총 개수: {} 로드 완료", tuple.getT1().getMaterials().size());
                    log.info("[Redis 성공] 설비 관련 데이터 총 개수: {} 로드 완료", tuple.getT2().getEquipmentInfo().size());
                });
    }

    // 스케줄 대상재를 등록하는 메서드 (단일 책임 원칙 적용)
    private Mono<Void> registerScheduleMaterials(Tuple2<RedisDataContainer, RefDataContainer> tuple) {
        RedisDataContainer scheduleData = tuple.getT1();
        RefDataContainer equipmentData = tuple.getT2();
        return registerService.registerScheduleMaterials(scheduleData, equipmentData)
                .then();
    }
}