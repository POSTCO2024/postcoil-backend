package com.postco.schedule.presentation.dto;

import com.postco.schedule.domain.ConstraintInsertionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConstraintInsertionDTO {
    private Long id;
    private Enum<ConstraintInsertionType> type;
    private String targetColumn;
    private String targetValue;
}
