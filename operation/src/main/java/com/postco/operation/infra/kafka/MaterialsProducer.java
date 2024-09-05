package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.exception.KafkaSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.postco.core.dto.MaterialDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialsProducer {

    private static final String TOPIC = "operation-material-data";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;  // JSON 변환용

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    public void sendMaterials(MaterialDTO.View materials) {
        log.info("Kafka Enabled: {}", kafkaEnabled);
        if (!kafkaEnabled) {
            log.warn("Kafka is disabled. Skipping the sending of materials data.");
            return;
        }

        try {
            // Map을 JSON 문자열로 변환
            String jsonMaterials = objectMapper.writeValueAsString(materials);

            // Kafka로 JSON 데이터를 전송하고 결과 로그 출력
            kafkaTemplate.send(TOPIC, jsonMaterials).addCallback(
                    success -> log.info("Sent materials data to Kafka: {}", jsonMaterials),
                    failure -> {
                        log.error("Failed to send materials data to Kafka. Exception: {}", failure.getMessage(), failure);
                        throw new KafkaSendException("Failed to send materials data", failure);
                    }
            );

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize MaterialsDTO.View", e);
            throw new RuntimeException("Failed to serialize MaterialsDTO.View", e);
        }
    }
}