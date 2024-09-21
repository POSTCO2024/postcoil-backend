package com.postco.operation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.postco.core.dto.DTO;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.entity.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        private String workStatus;
        private List<WorkInstructionItemDTO.View> items;
    }
}
