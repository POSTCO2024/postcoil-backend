package com.postco.core.redis.service;

import com.postco.core.redis.db.RedisDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RedisDatabaseSelector {
    private final RedisTemplate<String, Object> redisTemplate;

    public void selectMaterialDatabase(RedisDatabase database) {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()
                .select(database.getMaterialDatabase());

    }

    public void selectOrderDatabase(RedisDatabase database) {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()
                .select(database.getOrderDatabase());
    }
}
