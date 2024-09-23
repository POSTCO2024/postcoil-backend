package com.postco.operation.service;

import com.postco.operation.domain.entity.MaterialProgress;

import java.time.Duration;

public interface MaterialUpdateService {
    /**
     * 재료 진도 update 메서드
     *
     * @param materialId 재료 ID
     * @param newProgress 새로운 재료 상태
     */
    boolean updateMaterialProgress(Long materialId, MaterialProgress newProgress);

    // 재료 폭, 두께 업데이트
    boolean reduceThickAndWidth(Long materialId);

    // 재료 공정 업데이트
    boolean updateProcess(Long materialId);

    // 야드 업데이트
    boolean updateYard(Long materialId);
}
