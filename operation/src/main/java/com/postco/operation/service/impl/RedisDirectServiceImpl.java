package com.postco.operation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.cqrs.AbstractRedisCommandService;
import com.postco.operation.domain.repository.ColdStandardReductionRepository;
import com.postco.operation.domain.repository.EquipmentRepository;
import com.postco.operation.domain.repository.EquipmentStatusRepository;
import com.postco.operation.domain.repository.PlanProcessRepository;
import com.postco.operation.service.RedisDirectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisDirectServiceImpl extends AbstractRedisCommandService<Object> implements RedisDirectService {
    private static final String REDUCTION_KEY_PREFIX = "cold:standard:reduction:";
    private static final String EQUIPMENT_KEY_PREFIX = "equipment:";
    private static final String EQUIPMENT_STATUS_KEY_PREFIX = "equipment:status:";
    private static final String PLAN_PROCESS_KEY_PREFIX = "plan:process:";
    private static final int REDIS_DATABASE = 0;

    private final ColdStandardReductionRepository reductionRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;
    private final PlanProcessRepository planProcessRepository;

    public RedisDirectServiceImpl(ReactiveRedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, ColdStandardReductionRepository reductionRepository, EquipmentRepository equipmentRepository, EquipmentStatusRepository equipmentStatusRepository, PlanProcessRepository planProcessRepository) {
        super(redisTemplate, objectMapper);
        this.reductionRepository = reductionRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
        this.planProcessRepository = planProcessRepository;
    }

    @Override
    protected String getKeyPrefix() {
        // This method is not used in this implementation
        return "";
    }

    @Override
    protected Class<Object> getEntityClass() {
        return Object.class;
    }

    @Override
    public int getRedisDatabase() {
        log.info("database 선택 : {}", REDIS_DATABASE);
        return REDIS_DATABASE;
    }

    @Override
    public void saveColdStandardReductionData() {
        List<ColdStandardReduction> data = reductionRepository.findAll();
        saveDataList(data, REDUCTION_KEY_PREFIX);
    }

    @Override
    public void saveEquipmentData() {
        List<Equipment> data = equipmentRepository.findAll();
        saveDataList(data, EQUIPMENT_KEY_PREFIX);
    }

    @Override
    public void saveEquipmentStatus() {
        List<EquipmentStatus> data = equipmentStatusRepository.findAll();
        saveDataList(data, EQUIPMENT_STATUS_KEY_PREFIX);
    }

    @Override
    public void savePlanProcessData() {
        List<PlanProcess> data = planProcessRepository.findAll();
        saveDataList(data, PLAN_PROCESS_KEY_PREFIX);
    }

    private <T> void saveDataList(List<T> dataList, String keyPrefix) {
        dataList.forEach(data -> {
            String id = getIdFromData(data);
            String key = keyPrefix + id;
            saveData(key, data).block(); // 비동기 작업이 완료될 때까지 대기
        });
    }

    private <T> String getIdFromData(T data) {
        try {
            Method getIdMethod = data.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(data);
            return String.valueOf(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ID from data", e);
        }
    }

    // AbstractRedisCommandService의 saveData 메서드를 오버라이드하여 키 접두사를 사용
    @Override
    public Mono<Boolean> saveData(String key, Object data) {
        return super.saveData(key, data);
    }
}