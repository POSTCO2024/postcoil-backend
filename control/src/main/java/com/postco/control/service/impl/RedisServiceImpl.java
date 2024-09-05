package com.postco.control.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.control.service.RedisService;
import com.postco.core.common.redis.RedisDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisDataService redisDataService;

    @Override
    public Mono<Void> getAllMaterialsFromRedis() {
        return null;
    }

    @Override
    public Mono<Void> getNewMaterialsFromRedis() {
        return null;
    }
}
