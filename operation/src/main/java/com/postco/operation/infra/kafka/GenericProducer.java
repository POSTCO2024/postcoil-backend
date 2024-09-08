package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.exception.KafkaSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericProducer<T> {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    public void sendData(String topic, T data) {
        log.info("Kafka 활성화 여부 : {}", kafkaEnabled);
        if (!kafkaEnabled) {
            log.warn("[Kafka OFF] 카프카가 비활성화 되었습니다. 데이터를 보내지 않습니다.");
            return;
        }

        try {
            String jsonData = objectMapper.writeValueAsString(data);

            kafkaTemplate.send(topic, jsonData).addCallback(
                    success -> log.info("[Kafka ON] Kafka로 데이터를 전송합니다. TOPIC {}: {}", topic, jsonData),
                    failure -> {
                        log.error("[Kafka 실패] TOPIC :  {} 에 전송을 실패 했습니다. Exception: {}", topic, failure.getMessage(), failure);
                        throw new KafkaSendException("Failed to send data to " + topic, failure);
                    }
            );

        } catch (JsonProcessingException e) {
            log.error("[Kafka 실패] TOPIC  : {} 에 대해 데이터 직렬화를 실패했습니다.", topic, e);
            throw new RuntimeException("Failed to serialize data for topic " + topic, e);
        }
    }
}
