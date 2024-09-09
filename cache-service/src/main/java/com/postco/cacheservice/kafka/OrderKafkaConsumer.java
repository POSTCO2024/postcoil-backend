package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.CommandService;
import com.postco.cacheservice.service.impl.OrderCommandService;
import com.postco.core.config.kafka.KafkaMessageStrategy;
import com.postco.core.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaConsumer extends GenericKafkaConsumer<OrderDTO.View> {
    private final OrderCommandService orderCommandService; // Redis 저장 서비스
    private final ObjectMapper objectMapper;

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    @Override
    public void consumeMessage(String message) {
        try {
            // Kafka에서 받은 메시지를 OrderDTO.View로 변환
            OrderDTO.View order = objectMapper.readValue(message, OrderDTO.View.class);
            log.info("Received order data from Kafka: {}", order);

            // Redis에 저장
            saveData(order).subscribe(success -> {
                if (success) {
                    log.info("Order successfully saved in Redis: {}", order.getId());
                } else {
                    log.warn("Failed to save order in Redis: {}", order.getId());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize Kafka message", e);
        }
    }

    @Override
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    @Override
    public String getTopic() {
        return "operation-order-data"; // 토픽 이름 설정
    }

    @Override
    protected Mono<Boolean> saveData(OrderDTO.View order) {
        return orderCommandService.saveData(order); // Redis에 데이터 저장
    }
}
