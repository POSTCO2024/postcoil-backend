package com.postco.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkInstructionDTO implements DTO {
    private Long id;
    private String workCode;
    private String scheduleId;
    private Long expectedDuration;
    private Long actualDuration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String workStatus;
    private Long rejectedQuantity;
}
