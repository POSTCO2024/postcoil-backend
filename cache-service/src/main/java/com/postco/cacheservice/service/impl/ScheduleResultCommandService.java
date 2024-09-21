package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.core.redis.AbstractRedisCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleResultCommandService extends AbstractRedisCommandService<ScheduleResultDTO.View> {
    private static final String MATERIAL_KEY_PREFIX = "schedule:";
    private static final int MATERIAL_DATABASE = 0;

    public ScheduleResultCommandService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }
    @Override
    protected String getKeyPrefix() {
        return MATERIAL_KEY_PREFIX;
    }

    @Override
    protected Class<ScheduleResultDTO.View> getEntityClass() {
        return ScheduleResultDTO.View.class;
    }

    @Override
    public int getRedisDatabase() {
        return MATERIAL_DATABASE;
    }

    @Override
    public Mono<Boolean> saveData(ScheduleResultDTO.View data) {
        ReactiveHashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        // ObjectMapper를 사용해 data를 Map으로 변환
        Map<String, Object> rawDataMap = objectMapper.convertValue(data, new TypeReference<>() {
        });

        // 모든 값을 String으로 변환
        Map<String, String> dataMap = rawDataMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                ));

        // 'materials'는 따로 처리 (JSON으로 변환)
        try {
            String materialsJson = objectMapper.writeValueAsString(data.getMaterials());
            dataMap.put("materials", materialsJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing materials to JSON", e);
            return Mono.just(false);
        }

        String id = String.valueOf(data.getId());
        String key = getKeyPrefix() + id;

        return hashOperations.putAll(key, dataMap)
                .then(addProcessedId(id))
                .thenReturn(true);
    }
}
