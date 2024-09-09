package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.AbstractRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

public class MaterialRedisQueryService extends AbstractRedisQueryService<MaterialDTO.View> {
    public MaterialRedisQueryService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    protected String getKeyPrefix() {
        return "material:";
    }

    @Override
    protected Class<MaterialDTO.View> getEntityClass() {
        return MaterialDTO.View.class;
    }

    @Override
    public int getRedisDatabase() {
        return 0; // Assuming control service uses database 0
    }
}
