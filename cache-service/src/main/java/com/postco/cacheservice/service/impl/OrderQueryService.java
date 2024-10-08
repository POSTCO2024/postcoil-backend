package com.postco.cacheservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.OrderDTO;
import com.postco.core.redis.AbstractRedisQueryService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryService extends AbstractRedisQueryService<OrderDTO.View> {
    public OrderQueryService(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    @Override
    protected String getKeyPrefix() {
        return "order:";
    }

    @Override
    protected Class<OrderDTO.View> getEntityClass() {
        return OrderDTO.View.class;
    }

    @Override
    public int getRedisDatabase() {
        return 0;
    }
}

