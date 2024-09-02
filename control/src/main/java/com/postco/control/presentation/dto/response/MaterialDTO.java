package com.postco.control.presentation.dto.response;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MaterialDTO {
    private Long id;
    private String no;
    private String status;
    private String fCode;
    private String opCode;
    private String currProc;
    private String type;
    private String progress;
    private double outerDia;
    private double innerDia;
    private double width;
    private double thickness;
    private double length;
    private double weight;
    private double totalWeight;
    private String passProc;
    private String remProc;
    private String preProc;
    private String nextProc;
    private String storageLoc;
    private String yard;
    private String coilTypeCode;
    private String orderNo;
}
