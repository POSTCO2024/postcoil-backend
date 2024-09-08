package com.postco.control.service.impl;

import com.postco.control.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
//    private final RedisDataService redisDataService;

    @Override
    public Mono<Void> getAllMaterialsFromRedis() {
        return null;
    }

    @Override
    public Mono<Void> getNewMaterialsFromRedis() {
        return null;
    }
}
