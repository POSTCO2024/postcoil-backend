package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.entity.Materials;
import com.postco.cacheservice.service.MaterialDataService;
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
    private final MaterialDataService materialDataService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "operation-material-data", groupId = "redis-cache")
    public void consumeMaterials(String message) {
        try {
            Materials material = objectMapper.readValue(message, Materials.class);
            log.info("Received materials data: {}", material);

            materialDataService.saveMaterials(material)
                    .doOnSuccess(result -> {
                        if (result) {
                            log.info("Data successfully stored in Redis for ID: {}", material.getId());
                        } else {
                            log.warn("Failed to store data in Redis for ID: {}", material.getId());
                        }
                    })
                    .doOnError(e -> log.error("Error occurred while saving data to Redis for ID: {}", material.getId(), e))
                    .subscribe();
        } catch (JsonProcessingException e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
