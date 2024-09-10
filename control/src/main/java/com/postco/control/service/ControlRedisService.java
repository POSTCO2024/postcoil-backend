package com.postco.control.service;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ControlRedisService {
    Mono<List<MaterialDTO.View>> getAllMaterialsFromRedis();
    Mono<List<MaterialDTO.View>> getNewMaterialsFromRedis();

    Mono<List<OrderDTO.View>> getAllOrders();
    Mono<MaterialDTO.View> getMaterialById(Long materialId);
}
