package com.postco.operation.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColdStandardReductionDTO {
    private Long id;
    private String coilTypeCode;
    private String process;
    private Double thicknessReduction;
    private Double widthReduction;
    private Double temperature;
}
