package com.postco.operation.presentation.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardClientDTO {
    private List<TotalSupply> factoryDashboard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalSupply {
        private String process;
        private Integer totalGoalCoils;
        private Integer totalCompleteCoils;
        private Integer totalScheduledCoils;
    }


}
