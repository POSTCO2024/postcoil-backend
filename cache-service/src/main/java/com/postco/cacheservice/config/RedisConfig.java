package com.postco.cacheservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.entity.Materials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

//    @Value("${spring.redis.password}")
//    private String password;


    /**
     * 동기 레디스 설정
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host,port));
    }

    /**
     * 비동기식 reactive 레디스 설정
     */
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        // 비밀번호 설정 시 아래 코드 활성화
        // config.setPassword(password);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public <T> ReactiveRedisOperations<String, T> reactiveRedisOperations(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper, Class<T> valueType) {
        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(valueType);
        serializer.setObjectMapper(objectMapper);

        RedisSerializationContext.RedisSerializationContextBuilder<String, T> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, T> context = builder
                .key(new StringRedisSerializer())
                .value(serializer)
                .hashKey(new StringRedisSerializer())
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // 특정 타입에 대한 ReactiveRedisOperations 빈 생성 예시
    @Bean
    public ReactiveRedisOperations<String, Materials> materialsRedisOperations(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
        return reactiveRedisOperations(factory, objectMapper, Materials.class);
    }
}
