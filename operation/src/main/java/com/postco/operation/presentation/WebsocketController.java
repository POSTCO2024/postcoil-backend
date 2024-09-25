package com.postco.operation.presentation;

import com.postco.core.dto.CoilSupplyDTO;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {

    @MessageMapping("/coilData")
    @SendTo("/topic/coilData")
    public ClientDTO broadcast(ClientDTO clientDTO) {
        return clientDTO;
    }
}
