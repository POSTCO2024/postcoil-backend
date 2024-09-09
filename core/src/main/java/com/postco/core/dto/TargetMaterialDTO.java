package com.postco.core.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TargetMaterialDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create implements DTO {
        private Long id;
        private Long materialId;
        private Long materialNo;
        private String processPlan;
        private String orderNo;
        private String dueDate;
        private String rollUnitName;
        private String customerName;
        private String isError;
        private String errorType;
        private String remarks;
        private String coilTypeCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class View implements DTO {
        private Long id;
        private Long materialId;
        private String materialNo;
        private String orderNo;
        private double goalWidth;
        private double goalThickness;
        private double goalLength;
        private double weight;
        private String processPlan;
        private String dueDate;
        private String rollUnitName;
        private String customerName;
        private String remarks;
        private String coilTypeCode;
        private String isError;
        private String errorType;
        private String isErrorPassed;
    }



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Send implements DTO {
        private Long materialId;
        private String type;
        private String opCode;
        private String status;
        private String curProcCode;
        private String progress;
        private double thickness;
        private double width;
        private double weight;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
    }
}
