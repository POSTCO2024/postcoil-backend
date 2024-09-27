package com.postco.core.kafka.producer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RetryMessage {
    private final String topic;
    private final String key;
    private final String data;
}
