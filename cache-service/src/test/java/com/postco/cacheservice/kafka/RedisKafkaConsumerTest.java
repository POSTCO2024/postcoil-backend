package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=6379"
})

class RedisKafkaConsumerTest {
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private RedisKafkaConsumer redisKafkaConsumer;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushDb();
    }

    @AfterEach
    public void tearDown() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushDb();
    }

    @Test
    public void testWriteToRedis() {
        String key = "testKey";
        String value = "testValue";

        redisTemplate.opsForValue().set(key, value);

        String storedValue = (String) redisTemplate.opsForValue().get(key);
        assertThat(storedValue).isEqualTo(value);
    }
    @Test
    void 레디스_저장_테스트() throws JsonProcessingException {
        // given
        String testJson = "{ \"id\": 123, \"no\": \"CM692259\", \"status\": \"2\" }";

        // when
        redisKafkaConsumer.consumeMaterials(testJson);

        // then
        String redisKey = "material:123";
        String jsonResult = (String) redisTemplate.opsForValue().get(redisKey);

        assertThat(jsonResult).isNotNull();

        // JSON 문자열을 Map으로 다시 변환
        Map<String, Object> result = om.readValue(jsonResult, new TypeReference<>() {});

        assertThat(result.get("id")).isEqualTo(123);
        assertThat(result.get("no")).isEqualTo("CM692259");
        assertThat(result.get("status")).isEqualTo("2");
    }
}