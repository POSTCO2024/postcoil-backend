package com.postco.control.service;

import com.postco.control.presentation.dto.websocket.ControlClientDTO;
import com.postco.control.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebsocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(ControlClientDTO controlDto, WebSocketMessageType eventType) {
        String destination = "/topic/controlData";
        messagingTemplate.convertAndSend(destination, controlDto);
        log.info("Sent WebSocket message to {}: eventType={}, data={}", destination, eventType, controlDto);
    }
}
