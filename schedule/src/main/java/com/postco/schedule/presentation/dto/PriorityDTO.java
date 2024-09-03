package com.postco.schedule.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriorityDTO {
    private Long id;
    private String name;
    private Integer priorityOrder;
    private String applyMethod;
    private String targetColumn;
}
