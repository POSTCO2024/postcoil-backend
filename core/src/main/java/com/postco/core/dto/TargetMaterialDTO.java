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
        private Long materialId;
        private String materialNo;

        // 자주 조회되는 주문 데이터 저장
        private String orderNo;
        private double goalWidth;
        private double goalThickness;
        private double goalLength;
        private String dueDate;
        private String customerName;
        private String remarks;

        private String processPlan;    // 전체 공정

        // 작업대상재 시점에서 생기는 것
        private String rollUnitName;
        private String isError;
        private String errorType;
        private String isErrorPassed;
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
        private String dueDate;
        private String customerName;
        private String remarks;
        private String processPlan;    // 전체 공정
        private String rollUnitName;
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
