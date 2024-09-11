package com.postco.control.domain;

public enum TargetStatus {
    PENDING,   // 조업으로부터 온 재료(필터링 다 거친) => 스케쥴링으로 보낼 재료들
    SCHEDULED, // 스케쥴링 편성 완료
    IN_PROGRESS, // 스케쥴링 완료된 거 조업에서 진행중
    COMPLETED,   // 완료


}
