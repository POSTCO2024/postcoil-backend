package com.postco.cacheservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.dto.OrderDTO;
import com.postco.core.redis.db.SelectRedisDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class OrderDataService {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String ORDER_KEY_PREFIX = "order:";

    @SelectRedisDatabase(1)
    public Mono<Boolean> saveOrder(OrderDTO.View order) {
        ReactiveHashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        Map<String, Object> map = objectMapper.convertValue(order, new TypeReference<>() {});
        Map<String, String> orderMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));

        String key = ORDER_KEY_PREFIX + order.getId();
        return hashOperations.putAll(key, orderMap);
    }
}
