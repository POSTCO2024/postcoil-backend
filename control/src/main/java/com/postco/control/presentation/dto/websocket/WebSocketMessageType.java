package com.postco.control.presentation.dto.websocket;

public enum WebSocketMessageType {
    WORK_STARTED,
    WORK_COMPLETED,
    CAL_1,
    CAL_2,
    PCM_1,
    PCM_2,
    EGL_1,
    EGL_2,
    CGL_1,
    CGL_2;

    public static WebSocketMessageType fromString(String type) {
        try {
            return WebSocketMessageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
