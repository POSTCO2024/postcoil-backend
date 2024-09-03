package com.postco.schedule.presentation.dto;

import com.postco.schedule.domain.PriorityApplyMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriorityDTO {
    private Long id;
    private String name;
    private Integer priorityOrder;
    private Enum<PriorityApplyMethod> applyMethod;
    private String targetColumn;

}
