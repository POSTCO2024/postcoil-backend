package com.postco.operation.presentation.dto;

import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleSummaryDTO implements DTO {
    private String process;
    private int totalWorkInstructions;
    private int totalGoalCoils;
    private int totalCompleteCoils;
    private int totalScheduledCoils;
}
