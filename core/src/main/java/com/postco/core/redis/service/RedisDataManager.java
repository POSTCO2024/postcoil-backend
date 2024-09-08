package com.postco.core.redis.service;

import reactor.core.publisher.Mono;

public interface RedisDataManager<T, ID> {
    Mono<Void> save(T entity);
    Mono<T> findById(ID id);
    Mono<Void> update(T entity);
    Mono<Void> deleteById(ID id);
}
