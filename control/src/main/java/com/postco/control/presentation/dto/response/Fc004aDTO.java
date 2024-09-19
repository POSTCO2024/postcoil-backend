package com.postco.control.presentation.dto.response;

import com.postco.core.dto.DTO;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@ToString
public class Fc004aDTO {

    // 생산기한일
    public static class DueDate implements DTO {
        private String materialNo;
        private String dueDate;
    }

    // 에러재 비율
    public static class ErrorCount implements DTO {
        private long errorCount;
        private long normalCount;
    }

    // 품종 비율
    public static class CoilTypeCount implements DTO {
        private String coilType;
        private long coilCount;
    }

    // 고객사 비율
    public static class CustomerCount implements DTO {
        private String customerName;
        private long customerCount;
    }

    // 폭, 두께 분포
    public static class WidthThicknessCount implements DTO {
        private int widthRange;
        private int thicknessRange;
        private long count;
    }
}