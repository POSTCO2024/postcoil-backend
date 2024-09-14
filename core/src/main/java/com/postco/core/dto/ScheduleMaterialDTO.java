package com.postco.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleMaterialDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View implements DTO {
        private Long id; // materialId
        private Long targetId;  // Reference to TargetMaterialDTO
        private Long scheduleId;
        private String scheduleNo;
        private String workItemStatus;
        private List<Integer> sequence;
        private String isRejected;
        private Long expectedItemDuration; // 작업하는데 걸리는 시간
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
