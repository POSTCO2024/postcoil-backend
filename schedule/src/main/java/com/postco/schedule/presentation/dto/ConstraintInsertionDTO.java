package com.postco.schedule.presentation.dto;

import com.postco.core.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintInsertionDTO implements DTO {
    private Long id;
    private String type;
    private String targetColumn;
    private Double targetValue; // 2024-09-24 maxbort targetValue 타입 변경 String -> Double
}
