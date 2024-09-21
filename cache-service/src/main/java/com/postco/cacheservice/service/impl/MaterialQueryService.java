package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.redis.AbstractRedisQueryService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MaterialQueryService extends AbstractRedisQueryService<MaterialDTO.View> {
    public MaterialQueryService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
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
        return 0;
    }
}
