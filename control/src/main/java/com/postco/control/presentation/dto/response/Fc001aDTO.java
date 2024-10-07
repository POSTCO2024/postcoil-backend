package com.postco.control.presentation.dto.response;

import com.postco.core.dto.DTO;
import lombok.*;


public class Fc001aDTO{
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Table implements DTO {
        private String coilTypeCode;
        // private Long totalCnt;
        private Long proc1CAL;
        private Long proc2CAL;
        private Long proc1EGL;
        private Long proc2EGL;
        private Long proc1CGL;
        private Long proc2CGL;
        private Long proc1Packing;
        private Long proc2Packing;
    }
}
