package com.postco.control.service.impl;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.infra.kafka.TargetMaterialProducer;
import com.postco.control.service.ControlRedisService;
import com.postco.control.service.TargetMaterialService;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.TargetMaterialDTO;
import com.postco.core.utils.mapper.TargetMaterialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.postco.core.utils.mapper.TargetMaterialMapper.modelMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetMaterialServiceImpl implements TargetMaterialService {
    private final TargetMaterialRepository targetMaterialRepository;
    private final ExtractionFilterService extractionFilterService;
    private final ErrorFilterService errorFilterService;
    private final ControlRedisService controlRedisService;
    private final RollUnitService rollUnitService;
    private final TargetMaterialProducer targetMaterialProducer;

    @Transactional
    public Mono<List<TargetMaterialDTO.View>> processTargetMaterials(String processCode) {
        return Mono.zip(
                controlRedisService.getAllMaterialsFromRedis(),
                controlRedisService.getAllOrders()
        ).flatMap(tuple -> {
            List<MaterialDTO.View> materials = tuple.getT1();
            List<OrderDTO.View> orders = tuple.getT2();

            return Mono.fromCallable(() -> {
                // 추출 기준 적용
                List<MaterialDTO.View> filteredMaterials = extractionFilterService.applyExtractionCriteria(materials, processCode);

                // 작업대상재 매핑
                List<TargetMaterialDTO.View> targetMaterials = mapToTargetMaterials(filteredMaterials, orders);

                // 작업대상재 저장
                List<TargetMaterial> savedEntities = saveTargetMaterials(targetMaterials);

                // 에러 기준 적용
                errorFilterService.applyErrorCriteria(savedEntities, filteredMaterials, processCode);

                // 저장된 작업대상재 -> DTO 변환

                List<TargetMaterialDTO.View> savedDTOs = savedEntities.stream()
                        .map(entity -> modelMapper.map(entity, TargetMaterialDTO.View.class))
                        .collect(Collectors.toList());

                // Kafka로 저장된 작업대상재 발행
                savedDTOs.forEach(targetMaterialProducer::sendMaterials);

                return savedDTOs;
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }

    @Transactional
    public List<TargetMaterialDTO.View> mapToTargetMaterials(List<MaterialDTO.View> materials, List<OrderDTO.View> orders) {
        Map<Long, OrderDTO.View> orderMap = orders.stream()
                .collect(Collectors.toMap(OrderDTO.View::getId, Function.identity()));

        return materials.stream()
                .map(material -> {
                    OrderDTO.View order = orderMap.get(material.getOrderId());
                    TargetMaterialDTO.View targetMaterial = TargetMaterialMapper.mapToTargetMaterial(material, order);

                    // 롤 단위 설정
                    String rollUnitName = setRollUnit(material);
                    targetMaterial.setRollUnitName(rollUnitName);

                    return targetMaterial;
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public List<TargetMaterial> saveTargetMaterials(List<TargetMaterialDTO.View> targetMaterials) {
        List<TargetMaterial> entitiesToSave = new ArrayList<>();
        List<TargetMaterial> entitiesToUpdate = new ArrayList<>();

        for (TargetMaterialDTO.View targetMaterialDTO : targetMaterials) {
            targetMaterialRepository.findByMaterialId(targetMaterialDTO.getMaterialId())
                    .ifPresentOrElse(
                            existingEntity -> {
                                log.info("materialId가 {}인 기존 작업대상재 업데이트 중", targetMaterialDTO.getMaterialId());
                                modelMapper.map(targetMaterialDTO, existingEntity);
                                entitiesToUpdate.add(existingEntity);
                            },
                            () -> {
                                log.info("materialId가 {}인 새로운 작업대상재 생성 중", targetMaterialDTO.getMaterialId());
                                TargetMaterial newEntity = modelMapper.map(targetMaterialDTO, TargetMaterial.class);
                                entitiesToSave.add(newEntity);
                            }
                    );
        }

        log.info("업데이트할 엔티티 수: {}, 저장할 새로운 엔티티 수: {}", entitiesToUpdate.size(), entitiesToSave.size());

        List<TargetMaterial> updatedEntities = targetMaterialRepository.saveAll(entitiesToUpdate);
        List<TargetMaterial> savedEntities = targetMaterialRepository.saveAll(entitiesToSave);

        List<TargetMaterial> allEntities = new ArrayList<>(updatedEntities);
        allEntities.addAll(savedEntities);

        log.info("총 처리된 엔티티 수: {}", allEntities.size());
        return allEntities;
    }


    @Override
    public String setRollUnit(MaterialDTO.View material) {
        return rollUnitService.determineRollUnit(material.getThickness());
    }
}
