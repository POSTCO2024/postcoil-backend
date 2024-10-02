package com.postco.operation.service.impl.work;

import com.postco.operation.domain.repository.impl.CoilSupplyCustomImpl;
import com.postco.operation.domain.repository.impl.MaterialCustomImpl;
import com.postco.operation.domain.repository.impl.WorkInstructionCustomImpl;
import com.postco.operation.infra.kafka.WebsocketProducer;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.postco.operation.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientDashboardService {
    private final CoilSupplyCustomImpl coilSupplyCustom;
    private final WorkInstructionCustomImpl workInstructionCustom;
    private final WebsocketProducer websocketProducer;
    private final MaterialCustomImpl materialCustom;

    public void sendDashboardStartData(WebSocketMessageType eventType) {
        ControlClientDTO controlDto = ControlClientDTO.builder()
                .factoryDashboard(coilSupplyCustom.getTotalSupplyByProcess())
                .processDashboard(workInstructionCustom.getStatisticsInfo())
                .totalDashboard(materialCustom.getCurrentInfo())
                .build();

        websocketProducer.sendToControlWorkStart(String.valueOf(eventType), controlDto);
        log.info("Sent monitoring data for event: {}", eventType);
    }
    public void sendDashboardEndData(WebSocketMessageType eventType) {
        ControlClientDTO controlDto = ControlClientDTO.builder()
                .factoryDashboard(coilSupplyCustom.getTotalSupplyByProcess())
                .processDashboard(workInstructionCustom.getStatisticsInfo())
                .totalDashboard(materialCustom.getCurrentInfo())
                .build();
        websocketProducer.sendToControlWorkEnd(String.valueOf(eventType), controlDto);
        log.info("Sent monitoring data for event: {}", eventType);
    }

}
