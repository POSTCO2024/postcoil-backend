package com.postco.control.presentation.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Fc002DTO {
    private Long id;
    private Long materialId;
    private String materialNo;
    private String isError;
    private String errorType;
    private String fCode;
    private double weight;
    private String orderNo;
    private double goalWidth;
    private double goalThickness;
    private double goalLength;
    private String dueDate;
    private String customerName;
    private String remarks;
    private String type;
    private String cur_proc_code;
    private double thickness;
    private double width;
    private double totalWeight;
    private String remProc;
    private String storageLoc;
    private String yard;
}
