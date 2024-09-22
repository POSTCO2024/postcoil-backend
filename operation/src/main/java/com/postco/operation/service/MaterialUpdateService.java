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
    void updateMaterialProgress(Long materialId, MaterialProgress newProgress);
}
