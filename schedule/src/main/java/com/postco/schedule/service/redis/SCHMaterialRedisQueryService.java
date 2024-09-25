package com.postco.schedule.service.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.TargetMaterialDTO;
import com.postco.core.redis.AbstractRedisQueryService;
import com.postco.core.redis.cqrs.query.GenericRedisQueryService;
import com.postco.schedule.domain.SCHMaterial;
import com.postco.schedule.presentation.dto.SCHMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SCHMaterialRedisQueryService{
    private final GenericRedisQueryService redisQueryService;
    private final ObjectMapper objectMapper;

    /**
     * Redis에서 미편성된 코일 데이터를 조회하는 메서드.
     * Redis의 "unassigned_sch_material:"로 시작하는 모든 데이터를 조회하여 SCHMaterial 객체로 변환합니다.
     */
    public Mono<List<SCHMaterialDTO>> fetchAllUnassignedCoils() {
        return redisQueryService.fetchAllBySinglePrefix(
                        "unassigned_sch_material:",
                        SCHMaterialDTO.class
                )
                .collectList()
                .doOnNext(coils -> log.info("[Redis 성공] 모든 미편성된 코일 데이터를 Redis에서 불러왔습니다. 개수: {}", coils.size()))
                .doOnError(error -> log.error("미편성된 코일 데이터를 불러오는 중 오류 발생", error));
    }

    /**
     * 특정 ID의 미편성 코일 데이터를 조회하는 메서드.
     * Redis의 "unassigned_sch_material:{id}" 키로부터 데이터를 조회합니다.
     */
    public Mono<SCHMaterial> fetchUnassignedCoilById(String id) {
        return redisQueryService.fetchByKeyFromRedis("unassigned_sch_material:" + id, SCHMaterial.class)
                .doOnNext(coil -> log.info("[Redis 성공] 미편성된 코일 데이터를 Redis에서 불러왔습니다. ID: {}", id))
                .doOnError(error -> log.error("ID {}에 대한 미편성 코일 데이터를 불러오는 중 오류 발생", id, error));
    }

    /**
     * 여러 ID에 해당하는 미편성 코일 데이터를 조회하는 메서드.
     * Redis의 "unassigned_sch_material:"로 시작하는 여러 키에 해당하는 데이터를 조회합니다.
     */
    public Flux<SCHMaterial> fetchUnassignedCoilsByIds(List<String> ids) {
        return redisQueryService.fetchByKeysInSinglePrefix("unassigned_sch_material:", ids, SCHMaterial.class)
                .doOnNext(coil -> log.info("[Redis 성공] 미편성된 코일 데이터 중 하나를 Redis에서 불러왔습니다. ID: {}", coil.getId()))
                .doOnError(error -> log.error("미편성된 코일 데이터를 불러오는 중 오류 발생", error));
    }
}