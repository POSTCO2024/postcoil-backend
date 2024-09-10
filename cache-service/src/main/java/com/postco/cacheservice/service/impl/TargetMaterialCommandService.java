package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.AbstractRedisCommandService;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TargetMaterialCommandService extends AbstractRedisCommandService<TargetMaterialDTO.View> {
    private static final String TARGET_KEY_PREFIX = "target:";
    private static final int TARGET_DATABASE = 0;

    public TargetMaterialCommandService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    protected String getKeyPrefix() {
        return TARGET_KEY_PREFIX;
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
