package com.postco.operation.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.AbstractRedisCommandService;
import com.postco.operation.domain.entity.CoilSupply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CoilSupplyCommandService extends AbstractRedisCommandService<CoilSupply> {
    private static final String COIL_SUPPLY_KEY_PREFIX = "coil:supply:";
    private static final int COIL_SUPPLY_DATABASE = 0;

    public CoilSupplyCommandService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    protected String getKeyPrefix() {
        return COIL_SUPPLY_KEY_PREFIX;
    }

    @Override
    protected Class<CoilSupply> getEntityClass() {
        return CoilSupply.class;
    }

    @Override
    public int getRedisDatabase() {
        log.info("database 선택 : {}", COIL_SUPPLY_DATABASE);
        return COIL_SUPPLY_DATABASE;
    }
}
