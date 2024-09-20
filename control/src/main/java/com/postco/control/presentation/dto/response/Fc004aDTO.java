package com.postco.control.presentation.dto.response;

import com.postco.core.dto.DTO;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // 품종 비율
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class CoilTypeCount implements DTO {
        private String coilType;
        private long coilCount;
    }

    // 고객사 비율
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class CustomerCount implements DTO {
        private String customerName;
        private long customerCount;
    }

    // 폭, 두께 분포
    @Data
    @Builder
    @AllArgsConstructor
    @ToString
    public static class WidthThicknessCount implements DTO {
        private int widthRange;
        private int thicknessRange;
        private long count;
    }
}