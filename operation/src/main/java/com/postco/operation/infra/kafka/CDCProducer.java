package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.postco.core.dto.MaterialDTO;
import com.postco.operation.presentation.dto.CoilSupplyDTO;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CDCProducer {
    private final DataCaptureReceiverService captureReceiverService;

    @KafkaListener(topics = "operation.change-data.coil_supply")
    public void listenCoilSupplyChanges(String message) {
        processKafkaMessage(message, CoilSupplyDTO.class, "operation.change-data.coil_supply");
    }

    @KafkaListener(topics = "operation.change-data.work_instruction")
    public void listenWorkInstructionChanges(String message) {
        processKafkaMessage(message, WorkInstructionDTO.View.class, "operation.change-data.work_instruction");
    }

    @KafkaListener(topics = "operation.change-data.work_instruction_item")
    public void listenWorkInstructionItemChanges(String message) {
        processKafkaMessage(message, WorkInstructionItemDTO.View.class, "operation.change-data.work_instruction_item");
    }

    @KafkaListener(topics = "operation.change-data.materials")
    public void listenMaterialChanges(String message) {
        processKafkaMessage(message, MaterialDTO.View.class, "operation.change-data.materials");
    }

    private <T> void processKafkaMessage(String message, Class<T> targetType, String topic) {
        try {
            T dto = captureReceiverService.processKafkaMessage(message, targetType);
            log.info("[Kafka 성공] 토픽 {} 에서 {} DTO 처리 완료: {}", targetType.getSimpleName(), topic, dto);
        } catch (JsonProcessingException e) {
            log.error("[Kafka 실패] 토픽 {} 에서 Kafka 메시지 처리 중 에러 발생", topic, e);
        }
    }
}
