package com.postco.core.dto;

import lombok.*;

import java.time.LocalDateTime;

public class MaterialDTO {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View implements DTO {
        private Long id;
        private String no;
        private String status;
        private String factoryCode;
        private String opCode;
        private String currProc;
        private String materialType;
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
        private LocalDateTime createTime;

        // 주문 데이터 (자주 조회되는 핵심 데이터만 저장)
        private Long orderId;
        private String orderNo;
        private double goalThickness;
        private double goalWidth;
    }
}
