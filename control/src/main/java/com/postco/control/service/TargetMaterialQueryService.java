package com.postco.control.service;

import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.core.dto.RedisDataContainer;
import com.postco.core.dto.TargetMaterialDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TargetMaterialQueryService {
    /**
     * 저장된 작업 대상재와 그와 연관된 재료 및 주문을 매핑하는 메서드 (RedisDataContainer 에 매핑)
     */
    Mono<RedisDataContainer> mapTargetMaterialsWithRelatedData();

    /**
     * 저장된 작업 대상재와 그와 연관된 재료 및 주문을 매핑하는 메서드 (TargetViewDTO 에 매핑)
     */
    Mono<List<TargetViewDTO>> mapToTargetViewDTOs();

    /**
     * 레디스로 불러온 것을 프론트용 DTO 로 매핑하는 매서드
     */
    List<TargetViewDTO> convertToTargetViewDTOs(List<TargetMaterialDTO.View> targetMaterials, RedisDataContainer container);


    /**
     * 모든 작업 대상재를 불러오는 메서드
     */
    List<TargetMaterialDTO.View> getAllTargetMaterials();

}
