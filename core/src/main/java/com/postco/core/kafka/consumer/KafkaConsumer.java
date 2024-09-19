//package com.postco.core.kafka.consumer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class KafkaConsumer {
//    private final ObjectMapper objectMapper;
//
//    @Value("${feature-flags.kafka.enabled}")
//    private boolean kafkaEnabled;
//
//    @KafkaListener(topics = "#{'${kafka.topics}'.split(',')}", containerFactory = "kafkaListenerContainerFactory")
//    public void consume(@Payload String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
//        if (!kafkaEnabled) {
//            log.warn("[Kafka OFF] 카프카가 비활성화 되었습니다. 메시지를 처리하지 않습니다.");
//            return;
//        }
//
//        try {
//            log.info("[Kafka 수신] 토픽 {}에서 메시지를 수신했습니다.", topic);
//            // 여기에서 메시지 처리 로직을 구현합니다.
//            processMessage(topic, message);
//        } catch (Exception e) {
//            log.error("[Kafka 처리 실패] 토픽 {}의 메시지 처리를 실패했습니다. 예외: {}", topic, e.getMessage());
//            handleProcessingFailure(topic, message, e);
//        }
//    }
//
//    private void processMessage(String topic, String message) throws IOException {
//        // 토픽에 따라 다른 처리 로직을 구현할 수 있습니다.
//        // 예를 들어, 특정 DTO로 변환하거나 다른 서비스로 전달할 수 있습니다.
//        log.info("[Kafka 처리] 토픽 {}, 메시지: {}", topic, message);
//    }
//
//    private void handleProcessingFailure(String topic, String message, Exception e) {
//        // 실패한 메시지 처리에 대한 로직을 구현합니다.
//        // 예: 데드 레터 큐로 보내거나, 재시도 로직을 구현할 수 있습니다.
//        log.error("[Kafka 처리 실패 핸들링] 토픽 {}, 메시지: {}", topic, message, e);
//    }
//}