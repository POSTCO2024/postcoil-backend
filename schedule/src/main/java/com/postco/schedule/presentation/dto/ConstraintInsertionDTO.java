package com.postco.schedule.presentation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConstraintInsertionDTO {
    private Long id;
    private String type;
    private String targetColumn;
    private String targetValue;
}
