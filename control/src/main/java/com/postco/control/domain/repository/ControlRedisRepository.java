package com.postco.control.domain.repository;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ControlRedisRepository {
    Mono<List<MaterialDTO.View>> getAllMaterials();
    Mono<List<OrderDTO.View>> getAllOrders();
//    Mono<List<MaterialDTO.View>> getNewMaterials();
}
