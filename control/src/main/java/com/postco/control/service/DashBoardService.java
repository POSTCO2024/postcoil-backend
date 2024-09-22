package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.swing.plaf.PanelUI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final ControlRedisQueryService controlRedisQueryService;
    private final TargetMaterialRepository targetMaterialRepository;

    // 생산 기한일
    public Mono<List<Fc004aDTO.DueDate>> getDueDateInfo(String currProc) {
        return controlRedisQueryService.getRedisData()
                .flatMap(container -> {
                    // Redis에서 currProc에 맞는 materialId 추출
                    List<Long> materialIds = container.getMaterials().stream()
                            .filter(material -> currProc.equals(material.getCurrProc()))
                            .map(MaterialDTO.View::getId)
                            .collect(Collectors.toList());

                    // DB에서 해당 materialId에 해당하는 생산 마감일 조회
                    List<Object[]> results = targetMaterialRepository.findMaterialNoAndDueDateByMaterialIds(materialIds);

                    // 결과를 DTO로 변환
                    List<Fc004aDTO.DueDate> dueDates = results.stream()
                            .map(result -> new Fc004aDTO.DueDate((String) result[0], (String) result[1]))
                            .collect(Collectors.toList());

                    return Mono.just(dueDates);
                });
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
}
