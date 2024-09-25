package com.postco.operation.presentation.dto.websocket;

import lombok.Data;

@Data
public class WebSocketMessageDTO {
    private WebSocketMessageType type;
    private ClientDTO data;
}
