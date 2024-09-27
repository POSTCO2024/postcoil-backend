package com.postco.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleMaterialDTO implements DTO {
    private Long id;
    private String rollUnit;
    private String currProc;
    private Double temperature;
    private Double width;
    private Double thickness;
    private Long schedulePlanId;
    private String isScheduled;
    private int sequence;
    private String isRejected;
    private Long expectedDuration;
    private String workStatus;

    // 추가 필드 - maxbort 2024-09-19
    private double goalWidth;
    private double goalThickness;
    private String nextProc;

    // 추가 필드 - Sohyun Ahn 2024-09-19
    private String materialNo;

    // 추가 필드 -yerim kim 2024-09-22
    private Long materialId;
    private Long targetId;
}
