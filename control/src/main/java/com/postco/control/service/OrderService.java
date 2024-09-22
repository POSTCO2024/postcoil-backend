package com.postco.control.service;

import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final ControlRedisQueryService controlRedisQueryService;
    private final TargetMaterialRepository targetMaterialRepository;


    // 품종 비율
    public Mono<Map<String, Long>> getCoilTypesByCurrProc(String currProc) {
        return controlRedisQueryService.getRedisData()
            .map(container -> {
                List<MaterialDTO.View> materials = container.getMaterials();

                // currProc에 해당하는 품종별 카운트를 저장할 Map
                Map<String, Long> coilTypeCounts = new HashMap<>();

                for (MaterialDTO.View material : materials) {
                    // currProc이 일치하는지 확인
                    if (material.getCurrProc().equals(currProc)) {
                        String coilTypeCode = material.getCoilTypeCode();
                        coilTypeCounts.put(coilTypeCode, coilTypeCounts.getOrDefault(coilTypeCode, 0L) + 1);
                    }
                }

                return coilTypeCounts; // 품종별 카운트 반환
            });
    }


    // 고객사 비율
    public Mono<Map<String, Long>> getCustomerCountByProc(String currProc) {
        return controlRedisQueryService.getRedisData()
                .flatMap(container -> {
                    // Redis에서 공정에 맞는 materialId 추출
                    List<Long> materialIds = container.getMaterials().stream()
                            .filter(material -> currProc.equals(material.getCurrProc())) // 공정 필터링
                            .map(MaterialDTO.View::getId) // materialId 추출
                            .collect(Collectors.toList());

                    // DB에서 해당 materialIds에 해당하는 고객사 비율 조회
                    return Mono.fromSupplier(() -> {
                        List<Object[]> results = targetMaterialRepository.countByMaterialIdIn(materialIds); // 필터링된 ID로 고객사 조회
                        return results.stream()
                                .collect(Collectors.toMap(
                                        result -> (String) result[0], // 고객사 이름
                                        result -> (Long) result[1]    // 고객사 카운트
                                ));
                    });
                });
    }
}
