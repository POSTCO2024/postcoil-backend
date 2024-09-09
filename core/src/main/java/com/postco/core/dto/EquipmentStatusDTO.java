package com.postco.core.dto;

import java.time.LocalDateTime;

public class EquipmentStatusDTO {
    private Long id;
    private String eqStatus;
    private String isOperational;
    private LocalDateTime startTime;
    private LocalDateTime stopTime;
    private Long cumulativeTime;
    private Integer cumulativeAmount;
}
