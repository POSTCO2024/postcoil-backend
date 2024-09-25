package com.postco.operation.presentation.dto.websocket;

public enum WebSocketMessageType {
    WORK_STARTED,
    WORK_COMPLETED,
    BUFFERED_UPDATES,
    REJECTED,
    EMERGENCY_STOPPED
}
