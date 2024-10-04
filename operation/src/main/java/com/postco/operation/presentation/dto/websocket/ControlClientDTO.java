package com.postco.operation.presentation.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.postco.operation.domain.entity.MaterialProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ControlClientDTO {
    private List<TotalSupply> factoryDashboard;      // 공장별 작업대상재 대시보드 용
//    private List<StatisticsInfo> processDashboard;   // 공정별 작업대상재 대시보드 용
//    private List<CurrentInfo> totalDashboard; // 공정별 작업대상재 분석용

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalSupply {        // 편성된 모든 것에 대한 통계 수치
        private String process;
        private Integer totalGoalCoils;
        private Integer totalCompleteCoils;
        private Integer totalScheduledCoils;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatisticsInfo {     // 현재 작업중인 작업지시서에 대한 수치
        private String process;
        // 2024-09-27 추가됨
        private Integer workTotalCoils;
        private Integer workScheduledCoils;
        private Integer workTotalCompleteCoils;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime workStartTime;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CurrentInfo {
        private Map<String, Integer> nextProc = new HashMap<>(); // 초기화 추가\
        private Map<MaterialProgress, Integer> currProcess = new HashMap<>();
        private String currProc; // 초기화 추가
        //        private String equipmentStatus;
        private Long workInstructionId;

        public void addNextProc(String nextProc, int count) {
            this.nextProc.merge(nextProc, count, Integer::sum);
        }

        public void addCurrentProgress(MaterialProgress progress, int count) {
            this.currProcess.merge(progress, count, Integer::sum);
        }
    }





}