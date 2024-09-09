package com.postco.control.service.impl;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.service.RedisService;
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
    private final RedisService redisService;

    @Transactional
    public Mono<List<TargetMaterialDTO.View>> processTargetMaterials(String processCode) {
        return Mono.zip(
                redisService.getAllMaterialsFromRedis(),
                redisService.getAllOrders()
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

                // 최종 결과 반환
                return savedEntities.stream()
                        .map(entity -> modelMapper.map(entity, TargetMaterialDTO.View.class))
                        .collect(Collectors.toList());
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }

    private List<TargetMaterialDTO.View> mapToTargetMaterials(List<MaterialDTO.View> materials, List<OrderDTO.View> orders) {
        Map<Long, OrderDTO.View> orderMap = orders.stream()
                .collect(Collectors.toMap(OrderDTO.View::getId, Function.identity()));

        return materials.stream()
                .map(material -> {
                    OrderDTO.View order = orderMap.get(material.getOrderId());
                    return TargetMaterialMapper.mapToTargetMaterial(material, order);
                })
                .collect(Collectors.toList());
    }

    private List<TargetMaterial> saveTargetMaterials(List<TargetMaterialDTO.View> targetMaterials) {
        List<TargetMaterial> entities = targetMaterials.stream()
                .map(targetMaterial -> modelMapper.map(targetMaterial, TargetMaterial.class))
                .collect(Collectors.toList());

        List<TargetMaterial> savedEntities = targetMaterialRepository.saveAll(entities);
        log.info("Saved {} target materials", savedEntities.size());
        return savedEntities;
    }

    public Mono<MaterialDTO.View> getMaterialById(Long materialId) {
        return redisService.getMaterialById(materialId);
    }
}
