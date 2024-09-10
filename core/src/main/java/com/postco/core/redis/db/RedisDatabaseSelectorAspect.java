package com.postco.core.redis.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisDatabaseSelectorAspect {
    private final ReactiveRedisConnectionFactory connectionFactory;

    @Before("@annotation(selectRedisDatabase)")
    public void selectDatabase(JoinPoint joinPoint, SelectRedisDatabase selectRedisDatabase) {
        Integer databaseIndex = getDatabaseIndex(joinPoint, selectRedisDatabase.database());
        log.info("Selecting Redis database: {}", databaseIndex);

        if (connectionFactory instanceof LettuceConnectionFactory) {
            LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) connectionFactory;
            lettuceConnectionFactory.setDatabase(databaseIndex);
            lettuceConnectionFactory.resetConnection();
            log.info("Redis connection reset for database: {}", databaseIndex);
        }
    }

    private Integer getDatabaseIndex(JoinPoint joinPoint, String databaseExpression) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext(joinPoint.getTarget());
        return parser.parseExpression(databaseExpression).getValue(context, Integer.class);
    }
}
