package com.postco.control.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorStandardDTO {
    private String maxWidth;
    private String minWidth;
    private String maxThickness;
    private String minThickness;
    private String coilTypeCode;
    private String factoryCode;
    private String orderNo;
    private String remProc;
    private String rollUnit;
}
