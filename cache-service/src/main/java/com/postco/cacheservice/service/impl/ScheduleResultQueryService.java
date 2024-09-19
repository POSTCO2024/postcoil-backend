package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.core.redis.AbstractRedisQueryService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScheduleResultQueryService extends AbstractRedisQueryService<ScheduleResultDTO.View> {
    public ScheduleResultQueryService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    protected String getKeyPrefix() {
        return "schedule:";
    }

    @Override
    protected Class<ScheduleResultDTO.View> getEntityClass() {
        return ScheduleResultDTO.View.class;
    }

    @Override
    public int getRedisDatabase() {
        return 0;
    }
}
