package com.postco.control.presentation.dto.response;

import com.postco.core.dto.DTO;
import lombok.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Fc004aDTO {

    // 생산기한일
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class DueDate implements DTO {
        private String materialNo;
        private String dueDate;
    }

    // 에러재 비율
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class ErrorCount implements DTO {
        private long errorCount;
        private long normalCount;
    }
    
    
    // 품종/고객사 비율
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class Order implements DTO {
        private Map<String, Long> coilType;
        private Map<String, Long> customerName;
    }

    // 품종 비율
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class CoilTypeCount implements DTO {
//        private String coilType;
        private long coilTypeCount;
    }

    // 고객사 비율
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class CustomerCount implements DTO {
//        private String customerName;
        private long customerCount;
    }

    // 폭, 두께 분포
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class WidthThicknessCount implements DTO {
        private Map<Integer, Long> widthDistribution; // 폭 분포
        private Map<Double, Long> thicknessDistribution; // 두께 분포
    }
}