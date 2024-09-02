package com.postco.schedule.presentation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriorityDTO {
    private Long id;
    private String name;
    private Integer priorityOrder;
    private String applyMethod;
    private String targetColumn;

}
