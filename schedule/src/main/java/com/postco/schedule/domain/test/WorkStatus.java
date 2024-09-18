package com.postco.schedule.domain.test;

public enum WorkStatus {
    PENDING,      // 작업 대기 중
    IN_PROGRESS,  // 작업 진행 중
    COMPLETED,   // 작업 완료
    REJECTED     // 작업 거부됨 - 거부 코일에 대해서
}
