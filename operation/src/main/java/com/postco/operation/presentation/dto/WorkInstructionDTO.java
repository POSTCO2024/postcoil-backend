package com.postco.operation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.postco.core.dto.DTO;
import com.postco.operation.domain.entity.WorkStatus;

import java.time.LocalDateTime;
import java.util.List;

public class WorkInstructionDTO implements DTO {
    private Long id;
    private String workCode;
    private String scheduleCode;
    private Long expectedDuration;
    private Long actualDuration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private WorkStatus workStatus;
}
