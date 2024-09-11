package com.postco.operation.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanProcessDTO {
    private Long id;
    private String coilTypeCode;
    private String pcm;
    private String cal;
    private String egl;
    private String cgl;
    private String packing;
}