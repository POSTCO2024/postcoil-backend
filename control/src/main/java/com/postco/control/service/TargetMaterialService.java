package com.postco.control.service;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.TargetMaterialDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TargetMaterialService {
    /**
     * 재료와 주문 정보를 작업대상재로 매핑합니다.
     */
    Mono<List<TargetMaterialDTO.View>> processTargetMaterials(String processCode);

}