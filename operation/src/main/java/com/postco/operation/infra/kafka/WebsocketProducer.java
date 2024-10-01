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
    private static final String TOPIC = "operation-websocket-data";
    private final KafkaProducer genericProducer;

    public void sendToControl(String eventType, ControlClientDTO controlDto) {
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(TOPIC, key , controlDto);
    }

    public void sendToControlCurrSch(String eventType, ControlClientDTO controlClientDTO){
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(TOPIC, key, controlClientDTO);
    }
    public void sendToControltotalData(String eventType, ControlClientDTO controlClientDTO){
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(TOPIC, key, controlClientDTO);
    }


    public void sendToSchedule(OrderDTO.View orders) {
        String key = String.valueOf(orders.getId());
        genericProducer.sendData(TOPIC, key, orders);
    }
}
