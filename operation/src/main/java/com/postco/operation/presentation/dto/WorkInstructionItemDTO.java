package com.postco.operation.presentation.dto;

import com.postco.core.dto.DTO;
import com.postco.operation.domain.entity.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class WorkInstructionItemDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Create implements DTO {
        private Long materialId;
        private Long targetId;
        private WorkStatus workItemStatus;
        private int sequence;
        private String isRejected;
        private Long expectedItemDuration;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class View implements DTO {
        private Long id;
        private Long materialId;
        private Long targetId;
        private String workItemStatus;
        private int sequence;
        private String isRejected;
        private Long expectedItemDuration;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
