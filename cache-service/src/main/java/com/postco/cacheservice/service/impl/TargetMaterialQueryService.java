package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.TargetMaterialDTO;
import com.postco.core.redis.AbstractRedisQueryService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TargetMaterialQueryService extends AbstractRedisQueryService<TargetMaterialDTO.View> {
    public TargetMaterialQueryService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    protected String getKeyPrefix() {
        return "target:";
    }

    @Override
    protected Class<TargetMaterialDTO.View> getEntityClass() {
        return TargetMaterialDTO.View.class;
    }

    @Override
    public int getRedisDatabase() {
        return 0;
    }
}
