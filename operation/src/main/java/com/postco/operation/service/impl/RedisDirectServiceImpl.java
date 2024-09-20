package com.postco.operation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.DTO;
import com.postco.core.redis.AbstractRedisCommandService;
import com.postco.operation.domain.repository.*;
import com.postco.operation.presentation.dto.*;
import com.postco.operation.service.RedisDirectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
@Slf4j
public class RedisDirectServiceImpl extends AbstractRedisCommandService<Object> implements RedisDirectService {
    private static final int REDIS_DATABASE = 0;

    private final Map<Class<?>, String> keyPrefixMap = new HashMap<>();
    private final Map<Class<?>, Supplier<Iterable<?>>> repositoryMethodMap = new HashMap<>();

    public RedisDirectServiceImpl(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper,
                                  ColdStandardReductionRepository reductionRepository,
                                  EquipmentRepository equipmentRepository,
                                  EquipmentStatusRepository equipmentStatusRepository,
                                  PlanProcessRepository planProcessRepository) {
        super(redisTemplate, objectMapper);
        initKeyPrefixMap();
        initRepositoryMethodMap(reductionRepository, equipmentRepository, equipmentStatusRepository, planProcessRepository);
    }

    private void initKeyPrefixMap() {
        keyPrefixMap.put(ColdStandardReductionDTO.class, "cold:standard:reduction:");
        keyPrefixMap.put(EquipmentDTO.class, "equipment:");
        keyPrefixMap.put(EquipmentStatusDTO.class, "equipment:status:");
        keyPrefixMap.put(PlanProcessDTO.class, "plan:process:");
    }

    private void initRepositoryMethodMap(ColdStandardReductionRepository reductionRepository,
                                         EquipmentRepository equipmentRepository,
                                         EquipmentStatusRepository equipmentStatusRepository,
                                         PlanProcessRepository planProcessRepository) {
        repositoryMethodMap.put(ColdStandardReductionDTO.class, reductionRepository::findAll);
        repositoryMethodMap.put(EquipmentDTO.class, equipmentRepository::findAll);
        repositoryMethodMap.put(EquipmentStatusDTO.class, equipmentStatusRepository::findAll);
        repositoryMethodMap.put(PlanProcessDTO.class, planProcessRepository::findAll);
    }

    @Override
    protected String getKeyPrefix() {
        return "";
    }

    @Override
    protected Class<Object> getEntityClass() {
        return Object.class;
    }

    @Override
    public int getRedisDatabase() {
        return REDIS_DATABASE;
    }

    public Mono<Boolean> saveData(Object data) {
        String keyPrefix = keyPrefixMap.getOrDefault(data.getClass(), "");
        String id = getIdFromData(data);
        String key = keyPrefix + id;

        log.info("[Redis 처리 시작] 데이터 저장 시도 중: {}", data);

        return redisTemplate.opsForHash().putAll(key, convertToMap(data))
                .map(result -> true)
                .doOnNext(result -> logSaveResult(data.getClass().getSimpleName(), id, result))
                .onErrorResume(error -> {
                    log.error("[Redis 오류] 데이터 저장 실패: {}, 오류={}", data.getClass().getSimpleName(), error.getMessage());
                    return Mono.just(false);
                });
    }

    private Map<String, String> convertToMap(Object data) {
        Map<String, Object> map = objectMapper.convertValue(data, Map.class);
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            stringMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return stringMap;
    }

    private <T, D extends DTO> Mono<Void> saveEntities(Class<D> dtoClass) {
        Supplier<Iterable<?>> repositoryMethod = repositoryMethodMap.get(dtoClass);
        if (repositoryMethod == null) {
            return Mono.error(new IllegalArgumentException("Unsupported DTO class: " + dtoClass.getName()));
        }

        return Flux.fromIterable(repositoryMethod.get())
                .flatMap(entity -> {
                    try {
                        D dto = (D) entity.getClass().getMethod("convert", Class.class).invoke(entity, dtoClass);
                        return saveData(dto)
                                .onErrorResume(error -> {
                                    log.error("[Redis 오류] DTO 저장 실패: Entity={}, 오류={}", entity.getClass().getSimpleName(), error.getMessage());
                                    return Mono.just(false);
                                });
                    } catch (Exception e) {
                        log.error("[Redis 오류] DTO 변환 실패: Entity={}, 오류={}", entity.getClass().getSimpleName(), e.getMessage());
                        return Mono.just(false);
                    }
                })
                .then();
    }

    @Override
    public Mono<Boolean> saveColdStandardReductionData() {
        return saveEntities(ColdStandardReductionDTO.class).hasElement();
    }

    @Override
    public Mono<Boolean> saveEquipmentData() {
        return saveEntities(EquipmentDTO.class).hasElement();
    }

    @Override
    public Mono<Boolean> saveEquipmentStatus() {
        return saveEntities(EquipmentStatusDTO.class).hasElement();
    }

    @Override
    public Mono<Boolean> savePlanProcessData() {
        return saveEntities(PlanProcessDTO.class).hasElement();
    }

    private void logSaveResult(String entityType, String id, boolean success) {
        if (success) {
            log.info("[Redis 성공] {} 성공적으로 데이터를 저장했습니다. ID : {}", entityType, id);
        } else {
            log.warn("[Redis 실패] {} 데이터 저장에 실패했습니다. ID : {}", entityType, id);
        }
    }

    protected String getIdFromData(Object data) {
        try {
            String id = String.valueOf(data.getClass().getMethod("getId").invoke(data));
            log.info("[Redis] 생성된 키: {}", id);
            return id;
        } catch (Exception e) {
            log.error("ID 추출 실패", e);
            return "unknown";
        }
    }
}