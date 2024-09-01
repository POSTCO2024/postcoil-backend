package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisKafkaConsumer {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "operation-material-data", groupId = "redis-cache")
    public void consumeMaterials(String message) {
        try {
            Map<String, Object> materialsMap = objectMapper.readValue(message, new TypeReference<>() {});
            log.info("Received materials data: {}", materialsMap);

            String id = materialsMap.get("id").toString();
            String redisKey = "material:" + id;

            storeMaterialData(redisKey, materialsMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to process Kafka message", e);
        }
    }

    private void storeMaterialData(String redisKey, Map<String, Object> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(redisKey, jsonData);
            log.info("Data successfully stored in Redis with key: {}", redisKey);
        } catch (Exception e) {
            log.error("Failed to save data to Redis", e);
        }
    }
}
