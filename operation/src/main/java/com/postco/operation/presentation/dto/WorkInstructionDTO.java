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
import java.util.List;

public class WorkInstructionDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Create implements DTO {
        private String workNo;
        private Long scheduleId;
        private String scheduleNo;
        private String process;
        private String rollUnit;
        private int totalQuantity;
        private Long expectedDuration;
        private LocalDateTime startTime;
        private WorkStatus workStatus;
        private List<WorkInstructionItemDTO.Create> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class View implements DTO {
        private Long id;
        private String workNo;
        private Long scheduleId;
        private String scheduleNo;
        private String process;
        private String rollUnit;
        private int totalQuantity;
        private Long expectedDuration;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String schStatus;
        private List<WorkInstructionItemDTO.View> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message implements DTO {
        private Long workInstructionId;
        private String workNo;
        private Long scheduleId;
        private String scheduleNo;
        private String process;
        private String rollUnit;
        private int totalQuantity;
        private Long expectedDuration;

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime startTime;
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime endTime;

        private String schStatus;
        private List<WorkInstructionItemDTO.Message> items;
    }


}
