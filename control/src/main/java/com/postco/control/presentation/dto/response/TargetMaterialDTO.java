package com.postco.control.presentation.dto.response;


import com.postco.core.dto.DTO;
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
        private String materialNo;
        private String fCode;
        private double goalWidth;
        private double goalThickness;
        private double goalLength;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Table implements DTO {
        private String coilTypeCode;
        private Long totalCnt;
        private Long proc1CAL;
        private Long proc2CAL;
        private Long proc1EGL;
        private Long proc2EGL;
        private Long proc1CGL;
        private Long proc2CGL;
        private Long proc1Packing;
        private Long proc2Packing;
    }

    // 임의 데이터
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class View implements DTO {
        private int id;
        private String no;
        private int status;
        private String factoryCode;
        private String opCode;
        private String currProc;
        private String type;
        private String progress;
        private double outerDia;
        private double innerDia;
        private double width;
        private double thickness;
        private double length;
        private double weight;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
        private String coilTypeCode;
        private String orderNo;
    }
}