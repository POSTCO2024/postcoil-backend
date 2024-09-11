package com.postco.core.redis.cqrs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QueryService<T> {
    Mono<T> getData(String id);
    Flux<T> getAllData();
}
