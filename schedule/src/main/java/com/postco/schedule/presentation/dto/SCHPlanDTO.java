package com.postco.schedule.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class SCHPlanDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Create implements DTO {
        private String scheduleNo;
        private String process;
        private String rollUnit;
        private LocalDateTime planDate;
        private Long scExpectedDuration;
        private int quantity;
        private String isConfirmed;
        private List<SCHMaterialDTO> materials;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class View implements DTO {
        private Long id;
        private String scheduleNo;
        private String process;
        private String rollUnit;
        private LocalDateTime planDate;
        private Long scExpectedDuration;
        private int quantity;
        private String isConfirmed;
        private List<SCHMaterialDTO> materials;
    }
}
