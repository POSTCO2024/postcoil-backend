package com.postco.cacheservice.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RedisKafkaConsumerTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void testRedisConnection() {
        // Redis 연결 테스트: Redis에 임의의 키를 설정한 후 가져와서 확인하는 방식으로 연결을 확인
        String key = "pingTestKey";
        String value = "pong";

        redisTemplate.opsForValue().set(key, value);
        String storedValue = redisTemplate.opsForValue().get(key);

        assertThat(storedValue).isEqualTo(value);
    }

    @Test
    public void testWriteToRedis() {
        String key = "testKey";
        String value = "testValue";

        redisTemplate.opsForValue().set(key, value);

        String storedValue = (String) redisTemplate.opsForValue().get(key);
        System.out.println("Stored Value: " + storedValue);
        assertThat(storedValue).isEqualTo(value);
    }
}