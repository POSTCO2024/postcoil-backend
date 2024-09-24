package com.postco.websocket.service;


import com.postco.websocket.web.CoilData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CoilService {
    private final SimpMessagingTemplate messagingTemplate;

    public CoilService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    public CoilData processCoilData(String message) {
        log.info("메세지 요청 수신 완료 : {}", message);
        return CoilData.builder()
                .materialId(1L)
                .width(7000.0)
                .thickness(0.12)
                .nextCurr("1CAL")
                .build();
    }

    // 로직에서 바로 메시지 전송, 특정한 주체한테 못넘김
    public void directMessageToClient(String message) {
        messagingTemplate.convertAndSend("/topic/coilData", message);
    }

    // 정해진 시간마다 메시지 전송,
    @Scheduled(fixedRate = 3000)
    public void sendScheduleMessage() {
        log.info("scheduled Messaging");
        messagingTemplate.convertAndSend("/topic/coilData", "schduleMessage");
    }
}
