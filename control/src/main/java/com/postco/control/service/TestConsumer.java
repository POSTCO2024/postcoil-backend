package com.postco.control.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestConsumer {
    @KafkaListener(topics = "material", groupId = "operation")
    public void consumeMessage(String message) {
        log.info("수신 : {}",message);
    }
}
