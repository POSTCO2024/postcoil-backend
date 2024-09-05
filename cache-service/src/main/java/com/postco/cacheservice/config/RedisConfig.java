package com.postco.cacheservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
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
     * 비동기식 reactive 레디스 설정
     */
    @Primary
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public Jackson2HashMapper jackson2HashMapper() {
        return new Jackson2HashMapper(true);
    }

    /**
     * ReactiveRedisTemplate 설정
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        StringRedisSerializer stringValue = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        valueSerializer.setObjectMapper(objectMapper);  // ObjectMapper 설정

        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext(keySerializer)
                .key(keySerializer)
                .hashKey(keySerializer)
                .hashValue(stringValue)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }


    // ObjectMapper 설정
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
