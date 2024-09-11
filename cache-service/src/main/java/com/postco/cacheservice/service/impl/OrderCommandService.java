package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.cqrs.AbstractRedisCommandService;
import com.postco.core.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class OrderCommandService extends AbstractRedisCommandService<OrderDTO.View> {
    private static final String ORDER_KEY_PREFIX = "order:";
    private static final int ORDER_DATABASE = 0;

    public OrderCommandService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }
    @Override
    protected String getKeyPrefix() {
        return ORDER_KEY_PREFIX;
    }

    @Override
    public int getRedisDatabase() {
        log.info("database 선택 : {}", ORDER_DATABASE);
        return ORDER_DATABASE;
    }

    @Override
    protected Class<OrderDTO.View> getEntityClass() {
        return OrderDTO.View.class;
    }
}
