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

    @KafkaListener(topics = "operation-websocket-data", groupId = "control-group")
    public void consumeMessage(String message) {
        log.info("Received message: {}", message);

        try {
            // JSON 문자열을 JsonNode로 파싱
            JsonNode rootNode = objectMapper.readTree(message);

            // 각 대시보드 노드 추출
            JsonNode factoryDashboardNode = rootNode.get("factoryDashboard");
            JsonNode processDashboardNode = rootNode.get("processDashboard");
            JsonNode totalDashboardNode = rootNode.get("totalDashboard");

            // 각 노드를 DTO 리스트로 역직렬화
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

            // ControlClientDTO 객체 생성 및 필드 설정
            ControlClientDTO controlClientDTO = new ControlClientDTO();
            controlClientDTO.setFactoryDashboard(factoryDashboard);
            controlClientDTO.setProcessDashboard(processDashboard);
            controlClientDTO.setTotalDashboard(totalDashboard);

            // 필요한 작업 수행 (예: WebSocket으로 전송)
            processControlClientDTO(controlClientDTO);

        } catch (JsonProcessingException e) {
            log.error("JSON 처리 중 오류 발생", e);
        }
    }

    private void processControlClientDTO(ControlClientDTO data) {
        // 이벤트 타입 설정 (필요에 따라 조정)
        WebSocketMessageType eventType = WebSocketMessageType.WORK_STARTED; // 적절한 이벤트 타입 설정

        // WebSocket으로 메시지 전송
        websocketService.sendMessage(data, eventType);
        log.info("[WebSocket 전송 성공] 이벤트 타입: {}, 데이터: {}", eventType, data);
    }
}
