package com.postco.control.presentation.dto.response;


import com.postco.core.dto.DTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MaterialDTO implements DTO {
    private Long id;
    private Long materialId;
    private String materialNo;
    private String fCode;
    private String opCode;
    private String currProc;
    private double weight;
    private double goalWidth;
    private double goalThickness;
    private double goalLength;
    private String processPlan;
    private String orderNo;
    private String dueDate;
    private String rollUnit;
    private String customerName;
    private String isError;
    private String errorType;
    private String remarks;
    private String type;
    private String status;
    private String curProcCode;
    private String progress;
    private double thickness;
    private double width;
    private double totalWeight;
    private String passProc;
    private String remProc;
    private String preProc;
    private String nextProc;
    private String storageLoc;
    private String yard;
    private String coilTypeCode;

}
