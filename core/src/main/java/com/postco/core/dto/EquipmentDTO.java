package com.postco.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class EquipmentDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class View implements DTO {
        private Long id;
        private String eqCode;
        private String process;
        private double minWidthIn;
        private double maxWidthIn;
        private double minThicknessIn;
        private double maxThicknessIn;
        private double minWidthOut;
        private double maxWidthOut;
        private double minThicknessOut;
        private double maxThicknessOut;
        private double speed;
        private double maxWeight;
        private double tonForHour;

        private EquipmentStatusDTO status;
    }
}
