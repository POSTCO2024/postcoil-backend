package com.postco.operation.presentation.dto.websocket;

import com.postco.core.dto.CoilSupplyDTO;
import com.postco.core.dto.MaterialDTO;
import com.postco.operation.presentation.dto.EquipmentStatusDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private List<CoilSupplyDTO.Message> coilSupply;
    private List<WorkInstructionDTO.Message> workInstructions;
    private List<WorkInstructionItemDTO.Message> workItem;
    private List<EquipmentStatusDTO> equipmentStatus;
    private List<MaterialDTO.Message> materials;
    private Map<String, Integer> coilTypeCount;
    private String totalWorkTime;
}
