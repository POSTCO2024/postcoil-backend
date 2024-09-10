package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.AbstractRedisCommandService;
import com.postco.core.dto.MaterialDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class MaterialCommandService extends AbstractRedisCommandService<MaterialDTO.View> {
    private static final String MATERIAL_KEY_PREFIX = "material:";
    private static final int MATERIAL_DATABASE = 0;

    public MaterialCommandService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }
    @Override
    protected String getKeyPrefix() {
        return MATERIAL_KEY_PREFIX;
    }

    @Override
    protected Class<MaterialDTO.View> getEntityClass() {
        return MaterialDTO.View.class;
    }

    @Override
    public int getRedisDatabase() {
        log.info("database 선택 : {}", MATERIAL_DATABASE);
        return MATERIAL_DATABASE;
    }
}
