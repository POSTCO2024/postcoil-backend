package com.postco.control.service;

import com.postco.control.presentation.dto.response.Fc001aDTO;
import com.postco.control.service.impl.ProcessCounter;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NextProcessQueryService {
    private final ControlRedisQueryService controlRedisQueryService;


    public Mono<List<Fc001aDTO.Table>> getMaterialTable() {
        return controlRedisQueryService.getRedisData()
                .map(container -> {
                    List<MaterialDTO.View> materials = container.getMaterials();

                    // 결과를 저장할 HashMap
                    Map<String, Fc001aDTO.Table> resultMap = new HashMap<>();

                    // 가져온 재료들에 대해 countNextProc 호출
                    for (MaterialDTO.View material : materials) {
                        ProcessCounter.countNextProc(material, resultMap);
                    }

                    // Map의 값들을 List로 변환하여 반환
                    return new ArrayList<>(resultMap.values());
                });
        }
    }

