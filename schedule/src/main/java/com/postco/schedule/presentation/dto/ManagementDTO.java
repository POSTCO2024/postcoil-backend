package com.postco.schedule.presentation.dto;

import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagementDTO implements DTO {
    private List<PriorityDTO> priorities;
    private List<ConstraintInsertionDTO> constraintInsertions;
}
