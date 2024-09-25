package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
import com.postco.operation.presentation.dto.websocket.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebsocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ClientMapper clientMapper;

    public void sendUpdate(WorkInstruction workInstruction) {
//        ClientDTO clientDTO = clientMapper.mapToClientDTO(workInstruction);
//        messagingTemplate.convertAndSend("/topic/coil", clientDTO);
    }
}
