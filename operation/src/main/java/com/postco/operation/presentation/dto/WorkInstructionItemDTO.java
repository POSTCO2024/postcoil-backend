package com.postco.operation.presentation.dto;

import com.postco.operation.domain.entity.WorkStatus;

import java.time.LocalDateTime;

public class WorkInstructionItemDTO {
    private Long id;
    private Long materialId;
    private WorkStatus workItemStatus;
    private int sequence;
    private String isRejected;
    private Long expectedItemDuration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
