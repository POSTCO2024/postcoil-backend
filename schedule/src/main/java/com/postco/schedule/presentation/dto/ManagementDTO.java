package com.postco.schedule.presentation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ManagementDTO {
    private List<PriorityDTO> priorities;
    private List<ConstraintInsertionDTO> constraintInsertions;
}
