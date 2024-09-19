package com.postco.control.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetViewDTO {
    private Long targetId;
    private String processPlan;
    private String rollUnitName;
    private String isError;
    private String errorType;
    private String isErrorPassed;

    // 재료 및 주문
    private MaterialDTO.View material;
    private OrderDTO.View order;
}
