package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.entity.Materials;
import com.postco.cacheservice.service.MaterialDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisKafkaConsumer {
    private final MaterialDataService materialDataService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "operation-material-data", groupId = "redis-cache")
    public void consumeMaterials(String message) {
        try {
            // Kafka에서 수신한 메시지를 Materials 객체로 변환
            Materials material = objectMapper.readValue(message, Materials.class);
            log.info("Received materials data: {}", material);

            // Redis에 데이터를 저장
            Mono<Boolean> saveResult = materialDataService.saveMaterials(material);

            // 저장 결과 확인 및 로깅
            saveResult
                    .doOnSuccess(result -> {
                        if (result) {
                            log.info("Data successfully stored in Redis for ID: {}", material.getId());
                        } else {
                            log.warn("Failed to store data in Redis for ID: {}", material.getId());
                        }
                    })
                    .doOnError(e -> log.error("Error occurred while saving data to Redis for ID: {}", material.getId(), e))
                    .subscribe();  // 비동기 처리 구독
        } catch (JsonProcessingException e) {
            // 메시지 변환 실패 시 로깅
            log.error("Failed to process Kafka message", e);
        }
    }
}
