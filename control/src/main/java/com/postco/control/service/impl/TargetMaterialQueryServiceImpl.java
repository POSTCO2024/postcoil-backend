package com.postco.control.service.impl;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.control.service.TargetMaterialQueryService;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.RedisDataContainer;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TargetMaterialQueryServiceImpl implements TargetMaterialQueryService {
    private final TargetMaterialRepository targetMaterialRepository;
    private final ControlRedisQueryService controlRedisQueryService;
    private final ModelMapper modelMapper;

    @Override
    public Mono<RedisDataContainer> mapTargetMaterialsWithRelatedData() {
        return Mono.zip(
                Mono.fromCallable(this::getAllTargetMaterials),
                controlRedisQueryService.getRedisData()
        ).map(tuple -> {
            List<TargetMaterialDTO.View> targetMaterials = tuple.getT1();
            RedisDataContainer relatedData = tuple.getT2();

            return RedisDataContainer.builder()
                    .targetMaterials(targetMaterials)
                    .materials(relatedData.getMaterials())
                    .orders(relatedData.getOrders())
                    .build();
        }).doOnNext(result -> log.info("작업대상재 {} 개와 관련 데이터를 조회 성공 및  RedisDataContainer 매핑",
                result.getTargetMaterials().size()));
    }

    @Override
    public Mono<List<TargetViewDTO>> mapToTargetViewDTOs() {
        return Mono.zip(
                Mono.fromCallable(this::getAllTargetMaterials),
                controlRedisQueryService.getRedisData()
        ).map(tuple -> {
            List<TargetMaterialDTO.View> targetMaterials = tuple.getT1();
            RedisDataContainer relatedData = tuple.getT2();
            return convertToTargetViewDTOs(targetMaterials, relatedData);
        }).doOnNext(result -> log.info("작업대상재 {} 개와 관련 데이터를 조회 성공 및 TargetViewDTO 매핑", result.size()));
    }

    @Override
    public List<TargetMaterialDTO.View> getAllTargetMaterials() {
        List<TargetMaterial> targetMaterials = targetMaterialRepository.findAll();
        return targetMaterials.stream()
                .map(tm -> modelMapper.map(tm, TargetMaterialDTO.View.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TargetViewDTO> convertToTargetViewDTOs(List<TargetMaterialDTO.View> targetMaterials, RedisDataContainer container) {
        Map<Long, MaterialDTO.View> materialMap = container.getMaterials().stream()
                .collect(Collectors.toMap(MaterialDTO.View::getId, Function.identity()));
        Map<Long, OrderDTO.View> orderMap = container.getOrders().stream()
                .collect(Collectors.toMap(OrderDTO.View::getId, Function.identity()));

        return targetMaterials.stream()
                .map(targetMaterial -> {
                    MaterialDTO.View material = materialMap.get(targetMaterial.getMaterialId());
                    OrderDTO.View order = orderMap.get(material.getOrderId());

                    return TargetViewDTO.builder()
                            .material(material)
                            .order(order)
                            .targetId(targetMaterial.getId())
                            .processPlan(targetMaterial.getProcessPlan())
                            .rollUnitName(targetMaterial.getRollUnitName())
                            .isError(targetMaterial.getIsError())
                            .errorType(targetMaterial.getErrorType())
                            .isErrorPassed(targetMaterial.getIsErrorPassed())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
