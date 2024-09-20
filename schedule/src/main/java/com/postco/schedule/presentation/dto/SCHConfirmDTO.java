package com.postco.schedule.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

public class SCHConfirmDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class View implements DTO {
        private Long id;
        private String scheduleNo;
        private String process;
        private String rollUnit;
        private LocalDateTime confirmDate;
        private Long scExpectedDuration;
        private int quantity;
        private String confirmedBy;         // 컨펌한 사용자
        private String workStatus;
        private List<SCHMaterialDTO> materials;
    }
}
