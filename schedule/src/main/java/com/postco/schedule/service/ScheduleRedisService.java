package com.postco.schedule.service;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.TargetMaterialDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ScheduleRedisService {
    Mono<List<TargetMaterialDTO.View>> getAllTargetFromRedis();
    Mono<List<MaterialDTO.View>> getAllMaterialFromRedis();
}
