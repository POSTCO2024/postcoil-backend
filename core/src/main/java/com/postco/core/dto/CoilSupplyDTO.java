package com.postco.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message implements DTO{
        private Long coilSupplyId;
        private Long workInstructionId;
        private String workStatus;  // workInstruction 의 status 가져오기
        private int totalCoils;        // 전체 코일 수
        private int suppliedCoils;    // 공급 현황
        private int totalProgressed;   // 현재까지 진행된 현황
        private int totalRejects;     // 총 리젝 수
    }

}
