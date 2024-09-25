package com.postco.operation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoilSupplyDTO {
    private Long coilSupplyId;
    private Long workInstructionId;
    private String workStatus;  // workInstruction 의 status 가져오기
    private int totalCoils;        // 전체 코일 수
    private int suppliedCoils;    // 공급 현황
    private int totalProgressed;   // 현재까지 진행된 현황
    private int totalRejects;     // 총 리젝 수
}
