package com.postco.control.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface RedisService {
    Mono<Void> getAllMaterialsFromRedis();
    Mono<Void> getNewMaterialsFromRedis();

}
