package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.TargetMaterialDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface TargetMaterialService {

    /**
     * 해당 공정에 대해 추출기준 및 에러기준으로 작업대상재를 선별하는 메서드 입니다.
     * 이 메서드 안에서 추출 기준 서비스와 에러기준 서비스의 각 메서드를 호출하여 사용합니다.
     * @param processCode 해당 공정
     */
    Mono<List<TargetMaterialDTO.View>> processTargetMaterials(String processCode);
    /**
     * 재료와 주문 정보를 작업대상재로 매핑합니다.
     */
    List<TargetMaterialDTO.Create> mapToTargetMaterials(List<MaterialDTO.View> materials, List<OrderDTO.View> orders);

    /**
     * 작업대상재를 DB 에 저장하는 메서드 입니다.
     * @param targetMaterials 작업대상재 DTO
     */
    List<TargetMaterial> saveTargetMaterials(List<TargetMaterialDTO.Create> targetMaterials);

    /**
     * 추출된 작업대상재에 롤 단위를 매핑합니다.
     * @param orderOpt 작업대상재
     */
    String setRollUnit(Optional<OrderDTO.View> orderOpt);
    boolean isTargetMaterialExists(Long materialId, String materialNo);

}