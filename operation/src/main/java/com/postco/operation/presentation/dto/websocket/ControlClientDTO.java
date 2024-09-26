package com.postco.operation.presentation.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ControlClientDTO {
    private List<TotalSupply> factoryDashboard;      // 공장별 작업대상재 대시보드 용
    private List<StatisticsInfo> processDashboard;   // 공정별 작업대상재 대시보드 용

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalSupply {        // 편성된 모든 것에 대한 통계 수치
        private String process;
        private int totalGoalCoils;
        private int totalCompleteCoils;
        private int totalScheduledCoils;
        private LocalDateTime startTime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatisticsInfo {     // 현재 작업중인 작업지시서에 대한 수치
        private String process;
        // 2024-09-27 추가됨
        private int workTotalCoils;
        private int workScheduledCoils;
        private int workTotalCompleteCoils;
        private int workStartTime;
        private Map<String, Integer> nextProc;
        private Map<String, Integer> currentProgress;
        private String equipmentStatus;

        public void addNextProc(String nextProc, int count) {
            this.nextProc.merge(nextProc, count, Integer::sum);
        }

        public void addCurrentProgress(String progress, int count) {
            this.currentProgress.merge(progress, count, Integer::sum);
        }
    }
}