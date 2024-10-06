package com.postco.operation.infra.kafka;

import com.postco.core.dto.OrderDTO;
import com.postco.core.kafka.producer.KafkaProducer;
import com.postco.operation.domain.repository.impl.CoilSupplyCustomImpl;
import com.postco.operation.domain.repository.impl.WorkInstructionCustomImpl;
import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebsocketProducer {
    private final KafkaProducer genericProducer;

    private static final String TOPIC_START = "operation-websocket-data-start";
    private static final String TOPIC_END = "operation-websocket-data-end";
    private static final String DASHBOARD_TOPIC_PREFIX = "operation-dashboard-data-";
    private static final List<String> PROCESSES = Arrays.asList("1cal", "2cal", "1pcm", "2pcm", "1egl", "2egl", "1cgl", "2cgl");

    public void sendToControlWork(String eventType, ControlClientDTO controlDto, boolean isStart) {
        String topic = isStart ? TOPIC_START : TOPIC_END;
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(topic, key, controlDto);
    }

    public void sendToIndividualDashboardData(String process, AnalysisDashboardClientDTO analysisDashboardClientDTO) {
        String topic = DASHBOARD_TOPIC_PREFIX + process.toLowerCase();
        String key = "control:" + System.currentTimeMillis();
        genericProducer.sendData(topic, key, analysisDashboardClientDTO);
    }

    public void sendToSchedule(OrderDTO.View orders) {
        String key = String.valueOf(orders.getId());
        genericProducer.sendData(TOPIC_START, key, orders);
    }
}
