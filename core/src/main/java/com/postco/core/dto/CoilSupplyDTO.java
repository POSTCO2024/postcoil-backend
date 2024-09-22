package com.postco.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class CoilSupplyDTO {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Create implements DTO {
        private Long workInstructionId;
        private int totalCoils;
        private int suppliedCoils;
        private int totalRejects;
        private int totalProgressed;
        private int remainingCoils;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View implements DTO {
        private Long id;
        private Long workInstructionId;
        private int totalCoils;
        private int suppliedCoils;
        private int totalRejects;
        private int totalProgressed;
        private int remainingCoils;
    }

}
