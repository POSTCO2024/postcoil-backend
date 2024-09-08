package com.postco.core.redis.db;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RedisDatabaseSelectorAspect {
    private final ReactiveRedisConnectionFactory connectionFactory;

    @Before("@annotation(SelectRedisDatabase)")
    public void selectDatabase(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SelectRedisDatabase annotation = signature.getMethod().getAnnotation(SelectRedisDatabase.class);
        int databaseIndex = annotation.value();

        if(connectionFactory instanceof LettuceConnectionFactory) {
            LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) connectionFactory;
            lettuceConnectionFactory.setDatabase(databaseIndex);
            lettuceConnectionFactory.afterPropertiesSet();
         }
    }
}
