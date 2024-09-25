package com.postco.operation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postco.core.dto.DTO;
import com.postco.operation.domain.entity.coil.EquipmentStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EquipmentStatusDTO implements DTO {
    private Long equipmentStatus;
    private Long equipmentId;
    private String eqStatus;   // 상태 코드 (enum to string)
    private String isOperational;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private Long cumulativeTime;
    private Integer cumulativeAmount;

    public void setEqStatus(EquipmentStatusType status) {
        this.eqStatus = status != null ? status.name() : null;
    }

    public EquipmentStatusType getEqStatus() {
        return eqStatus != null ? EquipmentStatusType.valueOf(eqStatus) : null;
    }
}
