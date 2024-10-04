package com.postco.control.presentation.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardClientDTO {
    private List<StatisticsInfo> processDashboard;
     private List<CurrentInfo> totalDashboard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsInfo {
        private String process;
        private Integer workTotalCoils;
        private Integer workScheduledCoils;
        private Integer workTotalCompleteCoils;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")  // 날짜 형식 지정
        private LocalDateTime workStartTime;    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentInfo {
        private Map<String, Integer> nextProc = new HashMap<>();
        private Map<String, Integer> currProcess = new HashMap<>(); // Change to String for material progress
        private String currProc;
        private Long workInstructionId;
    }
}
