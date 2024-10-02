package com.postco.control.infra.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private ControlClientDTO parseMessage(String message) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(message);

        JsonNode factoryDashboardNode = rootNode.get("factoryDashboard");
        JsonNode processDashboardNode = rootNode.get("processDashboard");
        JsonNode totalDashboardNode = rootNode.get("totalDashboard");

        List<ControlClientDTO.TotalSupply> factoryDashboard = objectMapper.readValue(
                factoryDashboardNode.toString(),
                new TypeReference<List<ControlClientDTO.TotalSupply>>() {}
        );

        List<ControlClientDTO.StatisticsInfo> processDashboard = objectMapper.readValue(
                processDashboardNode.toString(),
                new TypeReference<List<ControlClientDTO.StatisticsInfo>>() {}
        );

        List<ControlClientDTO.CurrentInfo> totalDashboard = objectMapper.readValue(
                totalDashboardNode.toString(),
                new TypeReference<List<ControlClientDTO.CurrentInfo>>() {}
        );

        ControlClientDTO controlClientDTO = new ControlClientDTO();
        controlClientDTO.setFactoryDashboard(factoryDashboard);
        controlClientDTO.setProcessDashboard(processDashboard);
        controlClientDTO.setTotalDashboard(totalDashboard);

        return controlClientDTO;
    }

    private void processControlClientDTO(ControlClientDTO data, WebSocketMessageType eventType) {
        // WebSocket으로 메시지 전송
        websocketService.sendMessage(data, eventType);
        log.info("[WebSocket 전송 성공] 이벤트 타입: {}, 데이터: {}", eventType, data);
    }


}
