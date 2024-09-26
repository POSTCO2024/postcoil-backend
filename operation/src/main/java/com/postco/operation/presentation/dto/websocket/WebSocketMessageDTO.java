package com.postco.operation.presentation.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketMessageDTO {
    private WebSocketMessageType type;
    private MessageDTO data;
}
