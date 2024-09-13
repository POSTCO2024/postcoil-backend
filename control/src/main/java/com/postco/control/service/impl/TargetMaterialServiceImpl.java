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
import java.util.Collections;
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
                List<TargetMaterialDTO.Create> targetMaterials = mapToTargetMaterials(filteredMaterials, orders);
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
    public List<TargetMaterialDTO.Create> mapToTargetMaterials(List<MaterialDTO.View> materials, List<OrderDTO.View> orders) {
        Map<Long, OrderDTO.View> orderMap = orders.stream()
                .collect(Collectors.toMap(OrderDTO.View::getId, Function.identity()));

        return materials.stream()
                .map(material -> {
                    OrderDTO.View order = orderMap.get(material.getOrderId());
                    TargetMaterialDTO.Create targetMaterial = TargetMaterialMapper.mapToTargetMaterialCreate(material, order);

                    // 롤 단위 설정
                    String rollUnitName = setRollUnit(material);
                    targetMaterial.setRollUnitName(rollUnitName);

                    return targetMaterial;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String setRollUnit(MaterialDTO.View material) {
        return rollUnitService.determineRollUnit(material.getThickness());
    }

    @Transactional
    public List<TargetMaterial> saveTargetMaterials(List<TargetMaterialDTO.Create> targetMaterials) {
        List<TargetMaterial> newTargetMaterials = targetMaterials.stream()
                .filter(dto -> !isTargetMaterialExists(dto.getMaterialId(), dto.getMaterialNo()))
                .map(dto -> modelMapper.map(dto, TargetMaterial.class))
                .collect(Collectors.toList());

        if (!newTargetMaterials.isEmpty()) {
            List<TargetMaterial> newSavedEntities = new ArrayList<>();
            try {
                newSavedEntities = targetMaterialRepository.saveAll(newTargetMaterials);
                log.info("[저장 성공] 작업대상재 {} 개를 새롭게 저장했습니다. (총 제공된 재료: {} 개)",
                        newSavedEntities.size(), targetMaterials.size());
            } catch (Exception e) {
                log.error("[저장 실패] 작업대상재 저장 중 오류 발생: {}", e.getMessage(), e);
            }
            return newSavedEntities;
        }
        log.info("[저장 Skip] 새롭게 저장할 작업대상재가 없습니다.");
        return Collections.emptyList();
    }

    @Override
    public boolean isTargetMaterialExists(Long materialId, String materialNo) {
        return targetMaterialRepository.findByMaterialIdAndMaterialNo(materialId, materialNo).isPresent();
    }
}
