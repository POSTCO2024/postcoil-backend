package com.postco.schedule.service.impl;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.TargetMaterialDTO;
import com.postco.core.redis.CentralRedisService;
import com.postco.schedule.service.ScheduleRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleRedisServiceImpl implements ScheduleRedisService {
    private final CentralRedisService centralRedisService;

    @Override
    public Mono<List<TargetMaterialDTO.View>> getAllTargetFromRedis() {
        return centralRedisService.getAllData("target:*", TargetMaterialDTO.View.class)
                .collectList()
                .doOnNext(target -> log.info("[성공] 모든 작업대상재를 Redis로부터 불러왔습니다: {}", target));
    }

    @Override
    public Mono<List<MaterialDTO.View>> getAllMaterialFromRedis() {
        return centralRedisService.getAllData("material:*", MaterialDTO.View.class)
                .collectList()
                .doOnNext(materials -> log.info("[성공] 모든 재료들을 Redis로부터 불러왔습니다: {}", materials));
    }
}
