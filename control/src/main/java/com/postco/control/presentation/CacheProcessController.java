package com.postco.control.presentation;

import com.postco.control.service.ControlRedisService;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/redis")
@RequiredArgsConstructor
public class CacheProcessController {
    private final ControlRedisService controlRedisService;


    @GetMapping("/all")
    public Mono<ResponseEntity<List<MaterialDTO.View>>> getAllMaterials() {
        return controlRedisService.getAllMaterialsFromRedis()
                .map(materials -> {
                    log.info("Successfully retrieved materials: {}", materials);
                    return ResponseEntity.ok(materials);
                });
    }

    @GetMapping("/new")
    public Mono<ResponseEntity<List<MaterialDTO.View>>> getNewMaterials() {
        return controlRedisService.getNewMaterialsFromRedis()
                .map(materials -> {
                    log.info("Successfully retrieved new materials: {}", materials);
                    return ResponseEntity.ok(materials);
                });
    }


}
