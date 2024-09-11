package com.postco.operation.service;

public interface RedisDirectService {
    // 냉연 표준 감소기준 데이터 저장
    void saveColdStandardReductionData();

    // 설비 데이터 저장
    void saveEquipmentData();

    // 설비 상태 데이터 저장
    void saveEquipmentStatus();

    // 계획 공정 데이터 저장
    void savePlanProcessData();

}
