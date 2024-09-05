package com.postco.cacheservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.entity.Materials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class MaterialDataServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Mock
    private ReactiveHashOperations<String, String, String> reactiveHashOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MaterialDataService materialDataService;

    private Materials testMaterial;

    @BeforeEach
    void setUp() {
        // 테스트할 Materials 객체 초기화
        Materials testMaterial = new Materials(1L, "HPKL", "1CAL", "L", "29", "91197.22", "1EGL", "CM692259", 432.0, 1.0, 775.45, 1.6, 91197.22, 0.008503, 677.0, null, "1CAL1EGL101", null, "1PCM", "1CALA", null, null, "C");
    }

    @Test
    void 재료_저장_테스트_함수() {
        // Mock ObjectMapper의 동작 설정: Materials 객체를 Map으로 변환
        Map<String, Object> materialMap = new HashMap<>();
        materialMap.put("id", "12");
        materialMap.put("no", "HS922154");
        materialMap.put("status", "2");
        materialMap.put("opCode", "C");
        materialMap.put("currProc", "1PCM");

        // 서비스 메서드 호출
        Mono<Boolean> result = materialDataService.saveMaterials(testMaterial);

        // 결과 검증
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        // Redis와 ObjectMapper의 호출 확인
        verify(objectMapper, times(1)).convertValue(testMaterial, new TypeReference<Map<String, Object>>() {});
        verify(reactiveHashOperations, times(1)).putAll(anyString(), anyMap());
    }

    @Test
    void saveMaterials_shouldFailToStoreData() {
        // Mock ObjectMapper의 동작 설정: Materials 객체를 Map으로 변환
        Map<String, Object> materialMap = new HashMap<>();
        materialMap.put("id", "12");
        materialMap.put("no", "HS922154");
        materialMap.put("status", "2");

        // ObjectMapper가 객체를 Map으로 변환하도록

        // 서비스 메서드 호출
        Mono<Boolean> result = materialDataService.saveMaterials(testMaterial);

        // 결과 검증
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        // Redis와 ObjectMapper의 호출 확인
        verify(objectMapper, times(1)).convertValue(testMaterial, new TypeReference<Map<String, Object>>() {});
        verify(reactiveHashOperations, times(1)).putAll(anyString(), anyMap());
    }
}