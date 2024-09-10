package com.postco.operation.infra.kafka;

import com.postco.core.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProducer {
    private static final String TOPIC = "operation-order-data";
    private final GenericProducer<OrderDTO.View> genericProducer;

    public void sendOrders(OrderDTO.View orders) {
        genericProducer.sendData(TOPIC, orders);
    }
}
