package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.control.service.impl.ProcessCounter;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NextProcessQueryService {
    private final ControlRedisQueryService controlRedisQueryService;
    private final TargetMaterialRepository targetMaterialRepository;

    public Mono<List<Fc001aDTO.Table>> getMaterialTable(String currProc) {
        // DB에서 정상재(에러 아닌 것) 조회
        Set<Long> normalMaterialIds = targetMaterialRepository.findByIsError("N").stream()
                .map(TargetMaterial::getMaterialId)
                .collect(Collectors.toSet());

        // Redis에서 공정 데이터를 가져와서 필터링
        return controlRedisQueryService.getRedisData()
                .map(container -> {
                    Map<String, Fc001aDTO.Table> resultMap = new HashMap<>();

                    // Redis에서 데이터를 처리하여 품종별 차공정 개수 집계
                    container.getMaterials().forEach(material -> {
                        if (currProc.equals(material.getCurrProc()) && normalMaterialIds.contains(material.getId())) {
                            String coilTypeCode = material.getCoilTypeCode(); // 품종 코드
                            String nextProc = material.getNextProc(); // 차공정

                            // 결과 맵에 품종별로 차공정 개수 집계
                            resultMap.putIfAbsent(coilTypeCode, new Fc001aDTO.Table(coilTypeCode, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));
                            Fc001aDTO.Table tableEntry = resultMap.get(coilTypeCode);

                            // 차공정에 따라 개수 증가하는 로직을 분리한 메서드 호출
                            ProcessCounter.countNextProc(tableEntry, nextProc);
                        }
                    });

                    return new ArrayList<>(resultMap.values());
                });
    }
}

