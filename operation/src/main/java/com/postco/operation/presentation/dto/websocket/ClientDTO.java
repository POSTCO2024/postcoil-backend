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
    private List<CoilSupplyDTO> coilSupply;
    private List<WorkInstructionDTO.View> workInstructions;
    private List<WorkInstructionItemDTO.View> workItem;
    private List<EquipmentStatusDTO> equipmentStatus;
    private List<MaterialDTO.Message> materials;
    private Map<String, Integer> coilTypeCount;
    private String totalWorkTime;
}
