package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.control.service.impl.WidthThicknessCounter;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.RedisDataContainer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashBoardMaterialService {
    private final ControlRedisQueryService controlRedisQueryService;
    private final TargetMaterialRepository targetMaterialRepository;

    // 생산 기한일
    public Mono<List<Fc004aDTO.DueDate>> getDueDateInfo(String currProc) {
        // DB에서 정상재(에러 아닌 것) ID 조회
        Set<Long> normalMaterialIds = targetMaterialRepository.findByIsError("N").stream()
                .map(TargetMaterial::getMaterialId)
                .collect(Collectors.toSet());

        return controlRedisQueryService.getRedisData()
                .map(container -> container.getMaterials().stream()
                        .filter(material -> currProc.equals(material.getCurrProc())    // 해당 공정에 맞는 정상재인 재료 ID 조회
                                && normalMaterialIds.contains(material.getId()))
                        .map(material -> {
                            String dueDate = container.getOrders().stream()
                                    .filter(order -> order.getId().equals(material.getOrderId()))
                                    .findFirst()
                                    .map(order -> order.getDueDate().toString())     // 마감일 찾기
                                    .orElse(null);
                            return new Fc004aDTO.DueDate(material.getNo(), dueDate);
                        })
                        .filter(dueDate -> dueDate.getDueDate() != null)
                        .collect(Collectors.toList())
                );

    }



    // 에러재/정상재 비율
    public Mono<Fc004aDTO.ErrorCount> getErrorAndNormalCount(String currProc) {
        return controlRedisQueryService.getRedisData()
                .flatMap(container -> {
                    List<Long> materialIds = container.getMaterials().stream()
                            .filter(material -> currProc.equals(material.getCurrProc())) // 공정(currProc) 필터링
                            .map(MaterialDTO.View::getId) // ID 추출
                            .collect(Collectors.toList());

                    // Redis에서 필터링한 materialIds를 이용하여 DB에서 에러재/정상재 비율을 구함
                    long errorCount = targetMaterialRepository.countByMaterialIdInAndIsError(materialIds, "Y");
                    long normalCount = targetMaterialRepository.countByMaterialIdInAndIsError(materialIds, "N");

                    // 에러재/정상재 개수 반환
                    return Mono.just(new Fc004aDTO.ErrorCount(errorCount, normalCount));
                });
    }


    // 폭/두께 분포
    public Mono<Fc004aDTO.WidthThicknessCount> getWidthAndThicknessDistribution(String currProc) {
        Mono<List<TargetMaterial>> normalMaterialsMono = Mono.fromSupplier(() -> targetMaterialRepository.findByIsError("N"));

        return Mono.zip(controlRedisQueryService.getRedisData(), normalMaterialsMono)
                .map(tuple -> {
                    RedisDataContainer redisData = tuple.getT1();
                    List<TargetMaterial> normalMaterials = tuple.getT2();

                    Set<Long> normalMaterialIds = normalMaterials.stream()
                            .map(TargetMaterial::getMaterialId)
                            .collect(Collectors.toSet());

                    // 공정(currProc)과 정상재로 필터링된 자료 리스트 생성
                    List<MaterialDTO.View> filteredMaterials = redisData.getMaterials().stream()
                            .filter(material -> material.getCurrProc().equals(currProc))
                            .filter(material -> normalMaterialIds.contains(material.getId()))
                            .collect(Collectors.toList());

                    WidthThicknessCounter calculator = new WidthThicknessCounter();
                    return calculator.calculate(filteredMaterials);
                });
    }
    
    // 롤 단위 비율
    public Mono<Fc004aDTO.RollUnitCount> getRollUnitCountByCurrProc(String currProc) {
        return controlRedisQueryService.getRedisData()
                .flatMap(container -> {
                    List<Long> materialIds = container.getMaterials().stream()
                            .filter(material -> currProc.equals(material.getCurrProc())) // 공정(currProc) 필터링
                            .map(MaterialDTO.View::getId) // ID 추출
                            .collect(Collectors.toList());

                    // repository에서 조회
                    List<Object[]> results = targetMaterialRepository.countByRollUnitName(materialIds);
                    System.out.print(results);

                    Map<String, Long> rollUnitCountMap = new HashMap<>();

                    for (Object[] result : results) {
                        String rollUnitName = (String) result[0];
                        Long count = (Long) result[1];

                        // 롤 유닛 이름에 따라 카운트를 맵에 저장
                        rollUnitCountMap.put(rollUnitName, count);
                    }

                    // A와 B의 카운트를 각각 추출
                    long aCount = rollUnitCountMap.getOrDefault("A", 0L);
                    long bCount = rollUnitCountMap.getOrDefault("B", 0L);

                    // DTO 생성
                    Fc004aDTO.RollUnitCount rollUnitCount = Fc004aDTO.RollUnitCount.builder()
                            .ACount(aCount)
                            .BCount(bCount)
                            .build();

                    return Mono.just(rollUnitCount);
                });

    }


}
