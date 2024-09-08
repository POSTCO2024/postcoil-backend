package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {
    private static final String TOPIC = "op-order-data";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;  // JSON 변환용

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

}
