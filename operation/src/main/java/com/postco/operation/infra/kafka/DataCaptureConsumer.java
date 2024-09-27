package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.postco.core.dto.CoilSupplyDTO;
import com.postco.core.dto.MaterialDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import com.postco.operation.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCaptureConsumer {
    private final DataCaptureReceiverService captureReceiverService;

    private final Map<String, Map<String, Object>> changedDataMap = new HashMap<>();
    private Map<String, BiConsumer<String, String>> topicHandlers;

    @PostConstruct
    public void init() {
        topicHandlers = new HashMap<>();
        topicHandlers.put("operation.change-data.coil_supply",
                (message, topic) -> processMessage(message, CoilSupplyDTO.Message.class, "coilSupply", topic));
        topicHandlers.put("operation.change-data.work_instruction",
                (message, topic) -> processMessage(message, WorkInstructionDTO.Message.class, "workInstructions", topic));
        topicHandlers.put("operation.change-data.work_instruction_item",
                (message, topic) -> processMessage(message, WorkInstructionItemDTO.Message.class, "workItem", topic));
        topicHandlers.put("operation.change-data.materials",
                (message, topic) -> processMessage(message, MaterialDTO.Message.class, "materials", topic));
    }

    @KafkaListener(topics = {
            "operation.change-data.coil_supply",
            "operation.change-data.work_instruction",
            "operation.change-data.work_instruction_item",
            "operation.change-data.materials"
    })
    public void listenAllChanges(String message, String topic) {
        if (message == null || message.trim().isEmpty()) {
            log.warn("[Kafka 실패] 토픽 {} 에서 빈 메시지 수신", topic);
            return;
        }
        Optional.ofNullable(topicHandlers.get(topic))
                .ifPresentOrElse(handler -> handler.accept(message, topic),
                        () -> log.warn("알 수 없는 토픽: {}", topic));
    }

    private <T> void processMessage(String message, Class<T> targetType, String key, String topic) {
        try {
            T dto = captureReceiverService.processKafkaMessage(message, targetType);
            Map<String, Object> changedFields = getChangedFields(dto);
            changedDataMap.computeIfAbsent(key, k -> new HashMap<>()).putAll(changedFields);
            log.info("[Kafka 성공] 토픽 {} 에서 {} DTO 처리 완료: {}", topic, targetType.getSimpleName(), changedFields);
        } catch (JsonProcessingException e) {
            log.error("[Kafka 실패] 토픽 {} 에서 Kafka 메시지 처리 중 에러 발생", topic, e);
        }
    }

    private <T> Map<String, Object> getChangedFields(T dto) {
        // 여기서 DTO의 변경된 필드만 추출하는 로직 구현
        // 예: 이전 상태와 비교하거나, 특정 플래그를 확인하는 등의 방법 사용
        // 실제 구현은 DTO의 구조와 비즈니스 로직에 따라 달라질 수 있음
        return new HashMap<>();  // 임시 반환
    }

//    @EventListener
//    public void onWorkStart(WorkStartEvent event) {
//        log.info("작업 시작 이벤트 감지: {}", event);
//        sendBatchUpdate(WebSocketMessageType.WORK_STARTED);
//    }
//
//    @EventListener
//    public void onWorkEnd(WorkEndEvent event) {
//        log.info("작업 종료 이벤트 감지: {}", event);
//        sendBatchUpdate(WebSocketMessageType.WORK_ENDED);
//    }

    private void sendBatchUpdate(WebSocketMessageType messageType) {
//        WebSocketMessageDTO webSocketMessageDTO = new WebSocketMessageDTO(messageType, new ClientDTO(changedDataMap));
//        operationWebSocketService.sendMessage(webSocketMessageDTO);

        // 다른 서비스로 데이터 전송
//        kafkaTemplate.send("other-service-topic", webSocketMessageDTO);

        log.info("배치 업데이트 전송 완료. 메시지 타입: {}, 변경된 데이터: {}", messageType, changedDataMap);

        // 데이터 초기화
        changedDataMap.clear();
    }
}

// 기존 코드
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CDCProducer {
//    private final DataCaptureReceiverService captureReceiverService;
//    private final OperationWebSocketService operationWebSocketService;
//
//    @KafkaListener(topics = "operation.change-data.coil_supply")
//    public void listenCoilSupplyChanges(String message) {
//        processKafkaMessage(message, CoilSupplyDTO.Message.class,
//                "operation.change-data.coil_supply",
//                WebSocketMessageType.COIL_SUPPLY_UPDATED);
//    }
//
//    @KafkaListener(topics = "operation.change-data.work_instruction")
//    public void listenWorkInstructionChanges(String message) {
//        processKafkaMessage(message, WorkInstructionDTO.Message.class,
//                "operation.change-data.work_instruction",
//                WebSocketMessageType.WORK_INSTRUCTION_UPDATED);
//    }
//
//    @KafkaListener(topics = "operation.change-data.work_instruction_item")
//    public void listenWorkInstructionItemChanges(String message) {
//        processKafkaMessage(message, WorkInstructionItemDTO.Message.class,
//                "operation.change-data.work_instruction_item",
//                WebSocketMessageType.WORK_INSTRUCTION_ITEM_UPDATED);
//    }
//
//    @KafkaListener(topics = "operation.change-data.materials")
//    public void listenMaterialChanges(String message) {
//        processKafkaMessage(message, MaterialDTO.Message.class,
//                "operation.change-data.materials",
//                WebSocketMessageType.MATERIALS_UPDATED);
//    }
//
//    private <T> void processKafkaMessage(String message, Class<T> targetType, String topic, WebSocketMessageType type) {
//        if (message == null || message.trim().isEmpty()) {
//            log.warn("[Kafka 실패] 토픽 {} 에서 빈 메시지 수신", topic);
//            return;
//        }
//        try {
//            T dto = captureReceiverService.processKafkaMessage(message, targetType);
//            log.info("[Kafka 성공] 토픽 {} 에서 {} DTO 처리 완료: {}", targetType.getSimpleName(), topic, dto);
//
//            // WebSocket으로 데이터 전송
//            operationWebSocketService.sendMessage(dto, type);
//        } catch (JsonProcessingException e) {
//            log.error("[Kafka 실패] 토픽 {} 에서 Kafka 메시지 처리 중 에러 발생", topic, e);
//        }
//    }
//}
