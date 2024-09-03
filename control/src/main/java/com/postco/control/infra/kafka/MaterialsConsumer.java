package com.postco.control.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialsConsumer {
    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    private final ObjectMapper objectMapper;
    @KafkaListener(topics = "materials_info", groupId = "operation")
    public void consumeMaterials(String message) {
        log.info("Kafka Enabled: {}", kafkaEnabled);
        if (!kafkaEnabled) {
            log.warn("Kafka is disabled. Skipping the sending of materials data.");
            return;
        }

        try {
            Map<String, Object> materialsMap = objectMapper.readValue(message, new TypeReference<>() {});
            log.info("Received materials data: {}", materialsMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
