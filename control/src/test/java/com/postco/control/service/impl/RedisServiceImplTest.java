package com.postco.control.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.when;

class RedisServiceImplTest {
    @Mock private RedisDataService redisDataService;
    @InjectMocks private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 전체_재료_데이터_가져오기() {
        List<Map<Object, Object>> mock = List.of(
                Map.of("id", 1L, "no", "HS12345", "status", "2"),
                Map.of("id", 2L, "no", "HS54321", "status", "2")
        );

//        when(redisDataService.getAllMaterialData()).thenReturn(Mono.just(mock));
//
//        StepVerifier.create(redisService.getAllMaterialsFromRedis())
//                .verifyComplete();
//
//        // getAllMaterialData가 호출되었는지 확인
//        verify(redisDataService, times(1)).getAllMaterialData();
    }
}