package com.postco.control.presentation.dto.websocket;

public enum WebSocketMessageType {
    WORK_STARTED,
    WORK_COMPLETED;

    public static WebSocketMessageType fromString(String type) {
        try {
            return WebSocketMessageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
