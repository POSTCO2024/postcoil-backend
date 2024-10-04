package com.postco.operation.service.impl.work;

import com.postco.operation.domain.repository.impl.CoilSupplyCustomImpl;
import com.postco.operation.domain.repository.impl.MaterialCustomImpl;
import com.postco.operation.domain.repository.impl.WorkInstructionCustomImpl;
import com.postco.operation.infra.kafka.WebsocketProducer;
import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.postco.operation.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .build();

        websocketProducer.sendToControlWorkStart(String.valueOf(eventType), controlDto);
        log.info("Sent monitoring data for event: {}", eventType);
    }

    public void sendDashboardEndData(WebSocketMessageType eventType) {
        ControlClientDTO controlDto = ControlClientDTO.builder()
                .factoryDashboard(coilSupplyCustom.getTotalSupplyByProcess())
                .build();
        websocketProducer.sendToControlWorkEnd(String.valueOf(eventType), controlDto);
        log.info("Sent monitoring data for event: {}", eventType);
    }




    public void sendToIndividualDashboard1CALData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("1CAL"))
                .totalDashboard(materialCustom.getCurrentInfo("1CAL"))
                .build();
        websocketProducer.sendToIndividualDashboard1CALData(status);
    }

    public void sendToIndividualDashboard2CALData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("2CAL"))
                .totalDashboard(materialCustom.getCurrentInfo("2CAL"))
                .build();
        websocketProducer.sendToIndividualDashboard2CALData(status);
    }
    public void sendToIndividualDashboard1PCMData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("1PCM"))
                .totalDashboard(materialCustom.getCurrentInfo("1PCM"))
                .build();
        websocketProducer.sendToIndividualDashboard1PCMData(status);
    }
    public void sendToIndividualDashboard2PCMData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("2PCM"))
                .totalDashboard(materialCustom.getCurrentInfo("2PCM"))
                .build();
        websocketProducer.sendToIndividualDashboard2PCMData(status);
    }
    public void sendToIndividualDashboard1EGLData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("1EGL"))
                .totalDashboard(materialCustom.getCurrentInfo("1EGL"))
                .build();
        websocketProducer.sendToIndividualDashboard1EGLData(status);
    }
    public void sendToIndividualDashboard2EGLData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("2EGL"))
                .totalDashboard(materialCustom.getCurrentInfo("2EGL"))
                .build();
        websocketProducer.sendToIndividualDashboard2EGLData(status);
    }
    public void sendToIndividualDashboard1CGLData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("1CGL"))
                .totalDashboard(materialCustom.getCurrentInfo("1CGL"))
                .build();
        websocketProducer.sendToIndividualDashboard1CGLData(status);
    }
    public void sendToIndividualDashboard2CGLData() {
        AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisStaticsInfo("2CGL"))
                .totalDashboard(materialCustom.getCurrentInfo("2CGL"))
                .build();
        websocketProducer.sendToIndividualDashboard2CGLData(status);
    }




    public AnalysisDashboardClientDTO sendFirstStatus(String SchProcess) {
        AnalysisDashboardClientDTO firstStatus = AnalysisDashboardClientDTO.builder()
                .processDashboard(workInstructionCustom.getAnlysisAllStaticsInfo())
                .totalDashboard(materialCustom.getCurrentInfo(SchProcess))
                .build();
        return firstStatus;
    }
}
