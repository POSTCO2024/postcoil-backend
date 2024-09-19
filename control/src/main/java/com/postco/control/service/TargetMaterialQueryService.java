package com.postco.control.service;

import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.core.dto.RedisDataContainer;
import com.postco.core.dto.TargetMaterialDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TargetMaterialQueryService {
    /**
     * 저장된 작업 대상재와 그와 연관된 재료 및 주문을 매핑하는 메서드 (RedisDataContainer 에 매핑)
     * @return Mono<RedisDataContainer> 작업 대상재, 재료, 주문 정보가 포함된 RedisDataContainer
     */
    Mono<RedisDataContainer> mapTargetMaterialsWithRelatedData();

    /**
     * 저장된 작업 대상재와 그와 연관된 재료 및 주문을 매핑하는 메서드 (TargetViewDTO 에 매핑)
     * @return Mono<List<TargetViewDTO>> 작업 대상재, 재료, 주문 정보가 매핑된 TargetViewDTO 리스트
     */
    Mono<List<TargetViewDTO>> mapToTargetViewDTOs();

    /**
     * 모든 작업 대상재를 불러오는 메서드
     * @return List<TargetMaterialDTO.View> 모든 작업 대상재 리스트
     */
    List<TargetMaterialDTO.View> getAllTargetMaterials();

    /**
     * 정상 상태의 작업 대상재를 불러오는 메서드
     * @return List<TargetMaterialDTO.View> 정상 상태의 작업 대상재 리스트
     */
    List<TargetMaterialDTO.View> getNormalTargetMaterials();

    /**
     * 특정 공정의 정상 상태 작업 대상재를 불러오고 관련 정보를 매핑하는 메서드
     * @param currProc 현재 공정
     * @return Mono<List<TargetViewDTO>> 특정 공정의 정상 상태 작업 대상재와 관련 정보가 매핑된 TargetViewDTO 리스트
     */
    Mono<List<TargetViewDTO>> getNormalMaterialsByCurrProc(String currProc);
}
