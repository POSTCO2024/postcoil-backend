package com.postco.operation.presentation.dto.websocket;

public enum WebSocketMessageType {
    WORK_STARTED,
    WORK_COMPLETED,
    COIL_SUPPLY_UPDATED,
    WORK_INSTRUCTION_UPDATED,
    WORK_INSTRUCTION_ITEM_UPDATED,
    MATERIALS_UPDATED,
    REJECTED,
    EMERGENCY_STOPPED
}
