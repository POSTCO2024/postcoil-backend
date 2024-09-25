package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataCaptureReceiverService {
    private final ObjectMapper snakeCaseObjectMapper;

    public <R> R processKafkaMessage(String message, Class<R> targetType) throws JsonProcessingException {
        JsonNode jsonNode = snakeCaseObjectMapper.readTree(message);
        JsonNode payload = jsonNode.get("payload");

        return snakeCaseObjectMapper.treeToValue(payload, targetType);
    }
}