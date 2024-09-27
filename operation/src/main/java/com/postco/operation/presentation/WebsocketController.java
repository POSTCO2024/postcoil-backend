package com.postco.operation.presentation;

import com.postco.operation.presentation.dto.websocket.MessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {

    @MessageMapping("/coilData")
    @SendTo("/topic/coilData")
    public MessageDTO broadcast(MessageDTO messageDTO) {
        return messageDTO;
    }
}
