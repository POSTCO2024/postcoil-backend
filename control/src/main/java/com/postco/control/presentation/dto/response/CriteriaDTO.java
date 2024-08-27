package com.postco.control.presentation.dto.response;

import com.postco.core.dto.DTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CriteriaDTO implements DTO {
    private String processCode;
    private String criteriaGroup;
    private List<CriteriaDetailDTO> criteriaDetails;
}
