package com.postco.schedule.presentation.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SCHMaterialDTO implements DTO {
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
}
