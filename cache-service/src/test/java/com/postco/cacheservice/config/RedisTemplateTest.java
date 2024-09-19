package com.postco.cacheservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisTemplateTest {

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private ReactiveValueOperations<String, Object> valueOperations;
    private ReactiveHashOperations<String, String, Object> hashOperations;

    @BeforeEach
    public void setUp() {
        valueOperations = reactiveRedisTemplate.opsForValue();
        hashOperations = reactiveRedisTemplate.opsForHash();
    }

    @Test
    public void 키_값_저장_테스트() {
        String key = "testKey";
        String value = "testValue";

        // 데이터 저장
        Mono<Boolean> setResult = valueOperations.set(key, value);

        // 데이터 조회
        Mono<Object> getResult = valueOperations.get(key);

        // 데이터 저장 및 검증
        StepVerifier.create(setResult)
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(getResult)
                .assertNext(result -> assertEquals(value, result))
                .verifyComplete();
    }
}