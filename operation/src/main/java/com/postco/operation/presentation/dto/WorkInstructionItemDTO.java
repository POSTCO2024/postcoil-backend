package com.postco.operation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.postco.core.dto.DTO;
import com.postco.operation.domain.entity.WorkStatus;
import com.postco.operation.service.util.LocalDateTimeDeserializer;
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
        private Double initialThickness;
        private Double initialGoalWidth;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class View implements DTO {
        private Long id;
        private Long materialId;
        private String materialNo;
        private Long targetId;
        private String workItemStatus;
        private int sequence;
        private String isRejected;
        private Long expectedItemDuration;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double initialThickness;
        private Double initialGoalWidth;
        private Double temperature;
        private String preProc;
        private String nextProc;
        private Double weight;
        private Double length;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message implements DTO {
        private Long workItemId;
        private Long materialId;
        private Long targetId;
        private String workItemStatus;
        private int sequence;
        private String isRejected;
        private Long expectedItemDuration;
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime startTime;
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime endTime;
    }


}
