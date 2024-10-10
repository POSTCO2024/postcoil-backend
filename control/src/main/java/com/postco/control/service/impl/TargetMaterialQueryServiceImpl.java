package com.postco.control.service.impl;

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
import java.util.function.Supplier;
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
        }).doOnNext(result -> log.info("작업대상재 {} 개와 관련 데이터를 조회 성공 및 RedisDataContainer 매핑",
                result.getTargetMaterials().size()));
    }

    @Override
    public Mono<List<TargetViewDTO>> mapToTargetViewDTOs() {
        return getTargetViewDTOs(this::getAllTargetMaterials);
    }

    @Override
    public List<TargetMaterialDTO.View> getAllTargetMaterials() {
        return targetMaterialRepository.findAll().stream()
                .map(tm -> modelMapper.map(tm, TargetMaterialDTO.View.class))
                .collect(Collectors.toList());
    }

    public List<TargetMaterialDTO.View> getNormalTargetMaterials() {
        return targetMaterialRepository.findByIsError("N").stream()
                .map(tm -> modelMapper.map(tm, TargetMaterialDTO.View.class))
                .collect(Collectors.toList());
    }

    public Mono<List<TargetViewDTO>> getNormalMaterialsByCurrProc(String currProc) {
        return getTargetViewDTOs(this::getNormalTargetMaterials)
                .map(targetViews -> targetViews.stream()
                        .filter(view -> (currProc.equals(view.getMaterial().getCurrProc()) && (view.getIsError().equals("N"))))
                        .collect(Collectors.toList()));
    }

    private Mono<List<TargetViewDTO>> getTargetViewDTOs(Supplier<List<TargetMaterialDTO.View>> targetMaterialsSupplier) {
        System.out.println(targetMaterialsSupplier);
        return controlRedisQueryService.getRedisData()
                .map(redisDataContainer -> {
                    Map<Long, MaterialDTO.View> materialMap = redisDataContainer.getMaterials().stream()
                            .collect(Collectors.toMap(MaterialDTO.View::getId, Function.identity()));
                    Map<Long, OrderDTO.View> orderMap = redisDataContainer.getOrders().stream()
                            .collect(Collectors.toMap(OrderDTO.View::getId, Function.identity()));

                    return targetMaterialsSupplier.get().stream()
                            .map(targetMaterial -> {
                                MaterialDTO.View material = materialMap.get(targetMaterial.getMaterialId());
                                OrderDTO.View order = material != null ? orderMap.get(material.getOrderId()) : null;

                                return TargetViewDTO.builder()
                                        .material(material)
                                        .order(order)
                                        .targetId(targetMaterial.getId())
                                        .processPlan(targetMaterial.getProcessPlan())
                                        .rollUnitName(targetMaterial.getRollUnitName())
                                        .isError(targetMaterial.getIsError())
                                        .errorType(targetMaterial.getErrorType())
                                        .isErrorPassed(targetMaterial.getIsErrorPassed())
                                        .remarks(targetMaterial.getRemarks())
                                        .build();
                            })
                            .collect(Collectors.toList());
                });
    }

    // 페이징 실패
//    public Mono<Page<TargetMaterialDTO.View>> getNormalTargetMaterialsByPage(int page, String currProc) {
//        int pageSize = 10;
//        Pageable pageable = PageRequest.of(page, pageSize);
//        Page<TargetMaterial> targetMaterialPage = targetMaterialRepository.findByIsErrorIsN(pageable);
//        List<TargetMaterialDTO.View> targetMaterialDTOViews = targetMaterialPage.getContent().stream()
//                .map(tm -> modelMapper.map(tm, TargetMaterialDTO.View.class))
//                .collect(Collectors.toList());
//
//        getTargetViewDTOs(targetMaterialDTOViews).map(targetViews -> targetViews.stream()
//                .filter(view -> currProc.equals(view.getMaterial().getCurrProc()))
//                .collect(Collectors.toList()));
//
//        return new PageImpl<>(targetMaterialDTOViews, pageable, targetMaterialPage.getTotalElements());
//    }
}
