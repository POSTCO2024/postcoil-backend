package com.postco.control.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketReceiverService {
    private final ObjectMapper snakeCaseObjectMapper;

    public <R> R processKafkaMessage(String message, Class<R> targetType) throws JsonProcessingException {
        JsonNode jsonNode = snakeCaseObjectMapper.readTree(message);
        JsonNode payload = jsonNode.get("payload");

        return snakeCaseObjectMapper.treeToValue(payload, targetType);
    }
}
