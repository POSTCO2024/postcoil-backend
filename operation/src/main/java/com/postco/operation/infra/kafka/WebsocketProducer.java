package com.postco.operation.infra.kafka;

import com.postco.core.dto.OrderDTO;
import com.postco.core.kafka.producer.KafkaProducer;
import com.postco.operation.domain.repository.impl.CoilSupplyCustomImpl;
import com.postco.operation.domain.repository.impl.WorkInstructionCustomImpl;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebsocketProducer {
    private final CoilSupplyCustomImpl coilSupplyCustom;
    private final WorkInstructionCustomImpl workInstructionCustom;
    private static final String TOPIC = "operation-websocket-data-start";
    private static final String END_TOPIC = "operation-websocket-data-end";
    private final KafkaProducer genericProducer;

    public void sendToControlWorkStart(String eventType, ControlClientDTO controlDto) {
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(TOPIC, key , controlDto);
    }

    public void sendToControlWorkEnd(String eventType, ControlClientDTO controlClientDTO){
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(END_TOPIC, key, controlClientDTO);
    }


    public void sendToSchedule(OrderDTO.View orders) {
        String key = String.valueOf(orders.getId());
        genericProducer.sendData(TOPIC, key, orders);
    }
}
