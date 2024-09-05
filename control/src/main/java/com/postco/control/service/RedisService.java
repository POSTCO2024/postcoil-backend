package com.postco.control.service;

import com.postco.core.dto.MaterialDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface RedisService {
    Mono<List<MaterialDTO.View>> getAllMaterialsFromRedis();
    Mono<List<MaterialDTO.View>> getNewMaterialsFromRedis();

}
