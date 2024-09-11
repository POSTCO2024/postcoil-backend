package com.postco.operation.presentation.dto;

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
public class EquipmentStatusDTO {
    private Long id;
    private Long equipmentId;
    private EquipmentStatusType eqStatus;
    private String isOperational;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private Long cumulativeTime;
    private Integer cumulativeAmount;
}
