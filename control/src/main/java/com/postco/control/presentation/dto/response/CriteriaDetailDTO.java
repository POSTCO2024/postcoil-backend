package com.postco.control.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaDetailDTO {
    private String columnName;
    private String columnValue;
    private Long id;
}
