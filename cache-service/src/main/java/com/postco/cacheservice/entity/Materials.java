package com.postco.cacheservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Materials implements Serializable {
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

    public void updateProgress(String newProgress) {
        this.progress = newProgress;
    }
}
