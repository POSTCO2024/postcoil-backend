package com.postco.core.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Redis 에서 저장된 데이터를 가져올 때 사용하는 공통 DTO 클래스.
 * 여러 서비스가 공통적으로 사용하는 재료, 주문, 작업대상재 데이터를 관리합니다.
 *
 * 추가로, Redis 로 부터 전달받을 데이터가 필요하다면, 추가 가능
 */

@Data
@Builder
public class RedisDataContainer {
    private List<MaterialDTO.View> materials;
    private List<OrderDTO.View> orders;
    private List<TargetMaterialDTO.View> targetMaterials;
}
