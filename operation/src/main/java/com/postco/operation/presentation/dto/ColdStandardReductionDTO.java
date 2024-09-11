package com.postco.operation.presentation.dto;

import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColdStandardReductionDTO implements DTO {
    private Long id;
    private String coilTypeCode;
    private String process;
    private Double thicknessReduction;
    private Double widthReduction;
    private Double temperature;
}
