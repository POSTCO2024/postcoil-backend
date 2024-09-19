package com.postco.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class EquipmentInfoDTO {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class View implements DTO {
        private Long id;
        private String eqCode;
        private String process;
        private double speed;
        private double maxWeight;
        private double tonForHour;
        // 상태 추가 예정
    }
}
