package com.postco.operation.presentation.dto.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postco.core.dto.CoilSupplyDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientDTO {
    private WorkInstructionDTO.Message workInstructions;
//    private List<WorkInstructionItemDTO.Message> workItems; // 수정 240930 Sohyun Ahn
    private CoilSupplyDTO.Message coilSupply;

    private Map<String,Integer> countCoilTypeCode;

}
