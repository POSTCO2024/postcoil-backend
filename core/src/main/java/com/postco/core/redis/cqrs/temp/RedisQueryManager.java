package com.postco.core.redis.cqrs.temp;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RedisQueryManager<T, ID> {
    Mono<T> findById(ID id);
    Flux<T> findAll();
    Mono<Long> count();
}
