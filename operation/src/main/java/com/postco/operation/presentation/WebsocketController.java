package com.postco.operation.presentation;

import com.postco.core.dto.CoilSupplyDTO;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin(origins = "http://localhost:6050")
public class WebsocketController {

    @MessageMapping("/send")
    @SendTo("/topic/coil")
    public ClientDTO broadcast(ClientDTO clientDTO) {
        return clientDTO;
    }
}
