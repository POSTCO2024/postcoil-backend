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
        private String rollUnit;
        private String customerName;
        private String isError;
        private String errorType;
        private String remarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Send implements DTO {
        private Long materialId;
        private String type;
        private String op_code;
        private String status;
        private String cur_proc_code;
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
