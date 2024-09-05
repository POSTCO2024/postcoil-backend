package com.postco.operation.presentation.dto;

import com.postco.core.dto.DTO;
import lombok.*;

public class MaterialsDTO {
    @Getter
    @Setter
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Create implements DTO {
        private String no;
        private String status;
        private String fCode;
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View {
        private Long id;
        private String no;
        private String status;
        private String fCode;
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Update {
        private String status;
        private String opCode;
        private String currProc;
        private String type;
        private String progress;
        private double width;
        private double thickness;
        private String passProc;
        private String remProc;
        private String preProc;
        private String nextProc;
        private String yard;
    }
}
