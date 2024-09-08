package com.postco.control.presentation.dto.response;

import com.postco.control.domain.StatusEnum;
import com.postco.core.dto.DTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Fc001aDTO implements DTO {
    private Long id;
    private Long materialId;
    private String materialNo;
    private String fCode;

    private String type;
    private String op_code;
    private StatusEnum status;
    private String cur_proc_code;
    private String progress;
    private double thickness;
    private double width;
    private double weight;
    private double totalWeight;
    private String passProc;
    private String remProc;
    private String preProc;
    private String nextProc;
    private String storageLoc;
    private String yard;

    // order info
    private String orderNo;
    private double goalWidth;
    private double goalThickness;
    private double goalLength;
    private String processPlan;
    private String dueDate;
    private String customerName;
//    private String isError;
//    private String errorType;
    private String rollUnit;
    private String remarks;
}
