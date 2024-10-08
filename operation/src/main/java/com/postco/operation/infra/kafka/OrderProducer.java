package com.postco.operation.infra.kafka;

import com.postco.core.dto.OrderDTO;
import com.postco.core.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {
    private static final String TOPIC = "operation-order-data";
    private final KafkaProducer genericProducer;

    public void sendOrders(OrderDTO.View orders) {
        String key = String.valueOf(orders.getId());
        genericProducer.sendData(TOPIC, key, orders);
    }
}
