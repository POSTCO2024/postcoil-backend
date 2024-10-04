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

@Service
@RequiredArgsConstructor
@Slf4j
public class WebsocketProducer {
    private final CoilSupplyCustomImpl coilSupplyCustom;
    private final WorkInstructionCustomImpl workInstructionCustom;
    private static final String TOPIC = "operation-websocket-data-start";
    private static final String END_TOPIC = "operation-websocket-data-end";
    private static final String DASHBOARD_TOPIC_1CAL = "operation-dashboard-data-1cal";
    private static final String DASHBOARD_TOPIC_2CAL = "operation-dashboard-data-2cal";

    private static final String DASHBOARD_TOPIC_1PCM= "operation-dashboard-data-1pcm";
    private static final String DASHBOARD_TOPIC_2PCM= "operation-dashboard-data-2pcm";
    private static final String DASHBOARD_TOPIC_1EGL = "operation-dashboard-data-1egl";
    private static final String DASHBOARD_TOPIC_2EGL = "operation-dashboard-data-2egl";
    private static final String DASHBOARD_TOPIC_1CGL = "operation-dashboard-data-1cgl";
    private static final String DASHBOARD_TOPIC_2CGL = "operation-dashboard-data-2cgl";
    private final KafkaProducer genericProducer;

    public void sendToControlWorkStart(String eventType, ControlClientDTO controlDto) {
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(TOPIC, key , controlDto);
    }

    public void sendToControlWorkEnd(String eventType, ControlClientDTO controlClientDTO){
        String key = "control:" + eventType + ":" + System.currentTimeMillis();
        genericProducer.sendData(END_TOPIC, key, controlClientDTO);
    }

    public void sendToIndividualDashboard1CALData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_1CAL, key, analysisDashboardClientDTO);
    }

    public void sendToIndividualDashboard2CALData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_2CAL, key, analysisDashboardClientDTO);
    }
    public void sendToIndividualDashboard1PCMData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_1PCM, key, analysisDashboardClientDTO);
    }

    public void sendToIndividualDashboard2PCMData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_2PCM, key, analysisDashboardClientDTO);
    }
    public void sendToIndividualDashboard1EGLData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_1EGL, key, analysisDashboardClientDTO);
    }

    public void sendToIndividualDashboard2EGLData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_2EGL, key, analysisDashboardClientDTO);
    }
    public void sendToIndividualDashboard1CGLData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_1CGL, key, analysisDashboardClientDTO);
    }

    public void sendToIndividualDashboard2CGLData(AnalysisDashboardClientDTO analysisDashboardClientDTO){
        String key = "control:"+ System.currentTimeMillis();
        genericProducer.sendData(DASHBOARD_TOPIC_2CGL, key, analysisDashboardClientDTO);
    }



    public void sendToSchedule(OrderDTO.View orders) {
        String key = String.valueOf(orders.getId());
        genericProducer.sendData(TOPIC, key, orders);
    }
}
