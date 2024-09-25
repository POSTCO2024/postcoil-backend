package com.postco.schedule.service.impl;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.schedule.domain.SCHMaterial;
import com.postco.schedule.service.redis.SCHMaterialRedisQueryService;
import com.postco.schedule.service.redis.SCHMaterialRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RedisServiceImpl {

    private final SCHMaterialRedisService schMaterialRedisService;
    private final SCHMaterialRedisQueryService schMaterialRedisQueryService;

    public RedisServiceImpl(SCHMaterialRedisService schMaterialRedisService,
                            SCHMaterialRedisQueryService schMaterialRedisQueryService) {
        this.schMaterialRedisService = schMaterialRedisService;
        this.schMaterialRedisQueryService = schMaterialRedisQueryService;
    }

    // Redis에서 미편성된 코일 불러오기
    public List<SCHMaterial> fetchUnassignedCoils(String processCode, String rollUnit) {
        return Optional.ofNullable(schMaterialRedisQueryService.fetchUnassignedCoilsByCurrProcAndRollUnit(processCode, rollUnit).block())
                .map(unassignedCoilsFromRedis -> MapperUtils.mapList(unassignedCoilsFromRedis, SCHMaterial.class))
                .orElse(new ArrayList<>());
    }

    // Redis에 미편성된 코일 저장
    public Mono<Void> saveUnassignedCoils(List<SCHMaterial> unassignedCoils) {
        return Flux.fromIterable(unassignedCoils)
                .flatMap(coil -> schMaterialRedisService.saveData(coil)
                        .doOnSuccess(result -> log.info("미편성된 코일 저장 완료 - ID: {}", coil.getId()))
                        .doOnError(error -> log.error("Redis 저장 중 오류 발생 - ID: {}: {}", coil.getId(), error.getMessage())))
                .then();
    }

    // Redis에서 미편성된 코일 삭제
    public Mono<Void> deleteUnassignedCoils(List<String> unassignedCoilIds) {
        return Flux.fromIterable(unassignedCoilIds)
                .flatMap(id -> schMaterialRedisService.deleteData(id)
                        .doOnSuccess(result -> log.info("미편성된 코일 삭제 완료 - ID: {}", id))
                        .doOnError(error -> log.error("Redis 삭제 중 오류 발생 - ID: {}: {}", id, error.getMessage())))
                .then();
    }
}
