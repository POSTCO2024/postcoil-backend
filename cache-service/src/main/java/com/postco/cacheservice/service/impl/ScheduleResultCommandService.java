package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.core.redis.AbstractRedisCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

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
}
