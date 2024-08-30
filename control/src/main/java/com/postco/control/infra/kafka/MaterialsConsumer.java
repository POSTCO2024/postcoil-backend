package com.postco.control.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialsConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "materials_info", groupId = "operation")
    public void consumeMaterials(String message) {
        try {
            Map<String, Object> materialsMap = objectMapper.readValue(message, new TypeReference<>() {});
            log.info("Received materials data: {}", materialsMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
