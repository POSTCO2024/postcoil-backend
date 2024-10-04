package com.postco.control.infra.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.postco.control.presentation.dto.websocket.DashBoardClientDTO;
import com.postco.control.service.WebsocketService;
import com.postco.control.presentation.dto.websocket.ControlClientDTO;
import com.postco.control.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketConsumer {
    private final WebsocketService websocketService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @KafkaListener(topics = "operation-websocket-data-start", groupId = "control-group")
    public void consumeStartMessage(String message) {
        processMessage(message, WebSocketMessageType.WORK_STARTED);
    }

    @KafkaListener(topics = "operation-websocket-data-end", groupId = "control-group")
    public void consumeEndMessage(String message) {
        processMessage(message, WebSocketMessageType.WORK_COMPLETED);
    }
    @KafkaListener(topics = "operation-dashboard-data-1cal", groupId = "control-group")
    public void consume1CALMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.CAL_1);
    }

    @KafkaListener(topics = "operation-dashboard-data-2cal", groupId = "control-group")
    public void consume2CALMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.CAL_2);
    }
    @KafkaListener(topics = "operation-dashboard-data-1pcm", groupId = "control-group")
    public void consume1PCMMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.PCM_1);
    }

    @KafkaListener(topics = "operation-dashboard-data-2pcm", groupId = "control-group")
    public void consume2PCMMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.PCM_2);
    }
    @KafkaListener(topics = "operation-dashboard-data-1egl", groupId = "control-group")
    public void consume1EGLMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.EGL_1);
    }

    @KafkaListener(topics = "operation-dashboard-data-2egl", groupId = "control-group")
    public void consume2EGLMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.EGL_2);
    }
    @KafkaListener(topics = "operation-dashboard-data-1cgl", groupId = "control-group")
    public void consume1CGLMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.CGL_1);
    }

    @KafkaListener(topics = "operation-dashboard-data-2cgl", groupId = "control-group")
    public void consume2CGLMessage(String message) {
        processAnalysisMessage(message, WebSocketMessageType.CGL_2);
    }








    private void processMessage(String message, WebSocketMessageType eventType) {
        log.info("Received message: {}", message);
        try {
            ControlClientDTO controlClientDTO = parseMessage(message);
            processControlClientDTO(controlClientDTO, eventType);
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON: {}", message, e);
        } catch (Exception e) {
            log.error("Unexpected error processing message: {}", message, e);
        }
    }
    private void processAnalysisMessage(String message, WebSocketMessageType eventType) {
        log.info("Received message: {}", message);
        try {
            DashBoardClientDTO dashBoardClientDTO = parseAnalysisMessage(message);
            processDashboardClientDTO(dashBoardClientDTO, eventType);
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON: {}", message, e);
        } catch (Exception e) {
            log.error("Unexpected error processing message: {}", message, e);
        }
    }

    private ControlClientDTO parseMessage(String message) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(message);

        JsonNode factoryDashboardNode = rootNode.get("factoryDashboard");


        List<ControlClientDTO.TotalSupply> factoryDashboard = objectMapper.readValue(
                factoryDashboardNode.toString(),
                new TypeReference<List<ControlClientDTO.TotalSupply>>() {}
        );


        ControlClientDTO controlClientDTO = new ControlClientDTO();
        controlClientDTO.setFactoryDashboard(factoryDashboard);
        return controlClientDTO;
    }


    private DashBoardClientDTO parseAnalysisMessage(String message) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(message);

        JsonNode processDashboardNode = rootNode.get("processDashboard");
        JsonNode totalDashboardNode = rootNode.get("totalDashboard");


        List<DashBoardClientDTO.StatisticsInfo> processDashboard = objectMapper.readValue(
                processDashboardNode.toString(),
                new TypeReference<List<DashBoardClientDTO.StatisticsInfo>>() {}
        );

        List<DashBoardClientDTO.CurrentInfo> totalDashboard = objectMapper.readValue(
                totalDashboardNode.toString(),
                new TypeReference<List<DashBoardClientDTO.CurrentInfo>>() {}
        );

        DashBoardClientDTO dashBoardClientDTO = new DashBoardClientDTO();
        dashBoardClientDTO.setProcessDashboard(processDashboard);
        dashBoardClientDTO.setTotalDashboard(totalDashboard);

        return dashBoardClientDTO;
    }

    private void processControlClientDTO(ControlClientDTO data, WebSocketMessageType eventType) {
        // WebSocket으로 메시지 전송
        websocketService.sendMessage(data, eventType);
        log.info("[WebSocket 전송 성공] 이벤트 타입: {}, 데이터: {}", eventType, data);
    }
    private void processDashboardClientDTO(DashBoardClientDTO data, WebSocketMessageType eventType) {
        // WebSocket으로 메시지 전송
        websocketService.sendAnalysisMessage(data, eventType);
        log.info("[WebSocket 전송 성공] 이벤트 타입: {}, 데이터: {}", eventType, data);
    }


}
