package com.postco.control.presentation.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardClientDTO {
    private String process;
    private Integer totalGoalCoils;
    private Integer totalCompleteCoils;
    private Integer totalScheduledCoils;
}
