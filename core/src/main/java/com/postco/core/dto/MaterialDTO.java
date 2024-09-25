package com.postco.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MaterialDTO {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // error 수정 - maxbort 2024-09-19
    public static class View implements DTO {
        private Long materialId;
        private String materialNo;
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
        private double temperature;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
        private String coilTypeCode;
        private Long orderId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // error 수정 - maxbort 2024-09-19
    public static class Message implements DTO {
        private Long materialId;
        private String materialNo;
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
        private double temperature;
        private double totalWeight;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String storageLoc;
        private String yard;
        private String coilTypeCode;
        private Long orderId;
    }
}
