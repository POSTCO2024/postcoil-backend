package com.postco.core.redis.cqrs.temp;

import reactor.core.publisher.Mono;

public interface RedisCommandManager<T, ID> {
    Mono<Void> save(T entity);
    Mono<Void> update(T entity);
    Mono<Void> deleteById(ID id);
}
