package com.postco.schedule.service;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.RedisDataContainer;
import com.postco.core.dto.RefDataContainer;
import com.postco.core.dto.TargetMaterialDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ScheduleRedisService {
    /**
     * 스케쥴을 진행할 (작업대상재) 데이터를 불러오는 메소드 입니다.
     */
    Mono<RedisDataContainer> getScheduleData();

    /**
     * 스케쥴링에 필요한 참조 정적 데이터를 불러오는 메소드 입니다.
     */
    Mono<RefDataContainer> getReferenceData();

    // 외부 사용이 필요한 경우, 아래 메소드들을 주석 해제하고 사용하세요.
    // Mono<List<TargetMaterialDTO.View>> fetchAllTargetsFromRedis();
    // Mono<List<MaterialDTO.View>> fetchRelatedMaterials(List<TargetMaterialDTO.View> targetMaterials);
}
