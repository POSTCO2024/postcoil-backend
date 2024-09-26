package com.postco.operation.presentation.dto.websocket;

import com.postco.core.dto.CoilSupplyDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;

public class ClientDTO {
    private WorkInstructionDTO.Message workInstructions;
    private WorkInstructionItemDTO.Message workItems;
    private CoilSupplyDTO.Message coilSupply;
}
