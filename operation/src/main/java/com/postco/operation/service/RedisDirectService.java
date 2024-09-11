package com.postco.operation.service;

import reactor.core.publisher.Mono;

public interface RedisDirectService {
    // 냉연 표준 감소기준 데이터 저장
    Mono<Boolean> saveColdStandardReductionData();

    // 설비 데이터 저장
    Mono<Boolean> saveEquipmentData();

    // 설비 상태 데이터 저장
    Mono<Boolean> saveEquipmentStatus();

    // 계획 공정 데이터 저장
    Mono<Boolean> savePlanProcessData();

}
