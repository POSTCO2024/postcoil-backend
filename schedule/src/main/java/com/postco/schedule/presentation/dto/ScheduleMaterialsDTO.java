package com.postco.schedule.presentation.dto;

import com.postco.core.dto.DTO;
import lombok.*;

import java.util.List;

public class ScheduleMaterialsDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View{
        private Long id;
        private String no;
        private String status;
        private String fCode;
        private String opCode;
        private String currProc;
        private String type;
        private String progress;
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

        // 작업대상재 필드
        private double targetWidth;
        private double targetThickness;

        // 추가한 필드
        private double temperature;
        private double workTime;

        // 추가 처리순서
        private List<Integer> processOrder;
    }

    @Data
    @Builder
    public static class Create implements DTO {
        private Long id;
        private String no;
        private String status;
        private String fCode;
        private String opCode;
        private String currProc;
        private String type;
        private String progress;
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

        // 작업대상재 필드
        private double targetWidth;
        private double targetThickness;

        // 추가한 필드
        private double temperature;
        private double workTime;

        // 추가 처리순서
        private List<Integer> processOrder;
    }

}
