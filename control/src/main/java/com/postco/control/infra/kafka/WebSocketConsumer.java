package com.postco.control.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.control.service.WebsocketService;
import com.postco.control.presentation.dto.websocket.ControlClientDTO;
import com.postco.control.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketConsumer {

    private final ObjectMapper objectMapper;
    private final WebsocketService websocketService;

    @KafkaListener(topics = "#{__listener.getTopic()}", groupId = "#{__listener.getGroupId()}", autoStartup = "#{__listener.isKafkaEnabled()}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessages(List<String> records) {
        log.info(records.toString());
        records.forEach(record -> {
            log.info("Received message - value: {}", record);

            ControlClientDTO data = deserializeMessage(record);
            if (data != null) {
                String eventTypeStr = extractEventTypeFromKey(record); // 필요에 따라 key 추출 로직 조정
                WebSocketMessageType eventType = WebSocketMessageType.fromString(eventTypeStr);
                if (eventType != null) {
                    websocketService.sendMessage(data, eventType);
                    log.info("[WebSocket 전송 성공] 이벤트 타입: {}, 데이터: {}", eventType, data);
                } else {
                    log.warn("[WebSocket 전송 실패] 유효하지 않은 이벤트 타입: {}", eventTypeStr);
                }
            }
        });
    }

//    public void consumeMessages(List<String> records) {
//        Map<String, ControlClientDTO> dataMap = records.stream()
//                .map(record -> {
//                    ControlClientDTO data = deserializeMessage(record.value());
//                    return data != null ? Map.entry(record.key(), data) : null;
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (v1, v2) -> v2  // 중복 키의 경우 최신 값을 유지
//                ));
//
//        dataMap.forEach((key, data) -> {
//            String eventTypeStr = extractEventTypeFromKey(key);
//            WebSocketMessageType eventType = WebSocketMessageType.fromString(eventTypeStr);
//            if (eventType != null) {
//                websocketService.sendMessage(data, eventType);
//                log.info("[WebSocket 전송 성공] 이벤트 타입: {}, 데이터: {}", eventType, data);
//            } else {
//                log.warn("[WebSocket 전송 실패] 유효하지 않은 이벤트 타입: {}", eventTypeStr);
//            }
//        });
//    }

    private ControlClientDTO deserializeMessage(String message) {
        try {
            return objectMapper.readValue(message, ControlClientDTO.class);
        } catch (JsonProcessingException e) {
           // log.error("[역직렬화 실패] 메시지 역직렬화 중 오류 발생: {}", message, e);
            return null;
        }
    }

    private String extractEventTypeFromKey(String key) {
        // 키 형식이 "control:{eventType}:{timestamp}"인 경우 이벤트 타입 추출
        String[] parts = key.split(":");
        if (parts.length >= 2) {
            return parts[1];
        } else {
            log.warn("유효하지 않은 키 형식: {}", key);
            return null;
        }
    }

    // Kafka 설정을 위한 메서드들
    public boolean isKafkaEnabled() {
        // 실제 설정 값을 반환하도록 구현 필요
        return true;
    }

    public String getTopic() {
        return "operation-websocket-data";
    }

    public String getGroupId() {
        return "control-group";
    }
}
