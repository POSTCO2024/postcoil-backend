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

    // 메시지를 이벤트 타입에 따라 전송
    public void sendMessage(ControlClientDTO controlDto, WebSocketMessageType eventType) {
        String destination = getWebSocketDestination(eventType); // 이벤트에 맞는 목적지를 가져옴
        messagingTemplate.convertAndSend(destination, controlDto); // 해당 목적지로 메시지 전송
        log.info("Sent WebSocket message to {}: eventType={}, data={}", destination, eventType, controlDto);
    }

    // 이벤트 타입에 따라 WebSocket 목적지 경로 결정
    private String getWebSocketDestination(WebSocketMessageType eventType) {
        switch (eventType) {
            case WORK_STARTED:
                return "/topic/work-started"; // 작업 시작 이벤트 경로
            case WORK_COMPLETED:
                return "/topic/work-completed"; // 작업 완료 이벤트 경로
            default:
                return "/topic/controlData"; // 기본 경로
        }
    }
}
