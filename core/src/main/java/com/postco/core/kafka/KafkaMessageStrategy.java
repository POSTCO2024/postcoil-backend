package com.postco.core.kafka;

import reactor.core.publisher.Mono;

public interface KafkaMessageStrategy<T> {
    Class<T> getDataType();
    Mono<Boolean> processMessage(String message);
    String getTopic();
}
