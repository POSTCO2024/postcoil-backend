package com.postco.schedule.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.AbstractRedisCommandService;
import com.postco.schedule.domain.SCHMaterial;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SCHMaterialRedisService extends AbstractRedisCommandService<SCHMaterial> {

    public SCHMaterialRedisService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    protected String getKeyPrefix() {
        return "unassigned_sch_material:";
    }

    @Override
    protected Class<SCHMaterial> getEntityClass() {
        return SCHMaterial.class;
    }

    @Override
    public int getRedisDatabase() {
        return 0;  // 기본 Redis 데이터베이스 사용
    }
}