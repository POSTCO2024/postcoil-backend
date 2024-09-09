package com.postco.cacheservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class GenericKafkaConsumer<T> {
    @KafkaListener(topics = "#{__listener.topic}", groupId = "redis-cache", autoStartup = "#{__listener.kafkaEnabled}")
    public abstract void consumeMessage(String message);

    public abstract boolean isKafkaEnabled();
    public abstract String getTopic();
    protected abstract Mono<Boolean> saveData(T data);

}
