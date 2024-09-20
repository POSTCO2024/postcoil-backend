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
        Map<String, Object> map = objectMapper.convertValue(data, new TypeReference<>() {});
        Map<String, String> dataMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if ("materials".equals(entry.getKey()) && entry.getValue() != null) {
                try {
                    dataMap.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
                } catch (JsonProcessingException e) {
                    log.error("Error serializing materials", e);
                    return Mono.error(e);
                }
            } else {
                dataMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        String id = getIdFromData(data);
        String key = getKeyPrefix() + id;

        return hashOperations.putAll(key, dataMap)
                .then(addProcessedId(id))
                .thenReturn(true);
    }
}
