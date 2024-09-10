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


    /**
     * 작업 시작, 종료 관련 메서드
     */
    void startWork(Long workItemId);
    void finishWork(Long workItemId);


    /**
     * 재료 이송 관련 메서드
     * 이송 요청 -> 재로 진도 J(이송 중)으로 변경
     * 종료 상태 확인 -> 재료 진도 D(지시 대기)로 변경
     *
     */
    void requestTransfer(Long materialId);
    boolean checkTransferCompletion(Long materialId, Duration transferDuration);
}
