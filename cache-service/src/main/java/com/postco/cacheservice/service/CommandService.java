package com.postco.cacheservice.service;

import reactor.core.publisher.Mono;

public interface CommandService<T> {
    Mono<Boolean> saveData(T data);
    Mono<Boolean> updateData(String id, T data);
    Mono<Boolean> deleteData(String id);
}
