package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.impl.TargetMaterialCommandService;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetMaterialConsumer extends GenericKafkaConsumer<TargetMaterialDTO.View> {
    private final TargetMaterialCommandService targetCommandService;
    private final ObjectMapper objectMapper;

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    @Override
    public void consumeMessage(String message) {
        try {
            TargetMaterialDTO.View targetMaterial = objectMapper.readValue(message, TargetMaterialDTO.View.class);
            log.info("[Kafka 성공] 작업대상재 메세지 수신에 성공하였습니다. : {}", targetMaterial);

            // Redis에 저장
            saveData(targetMaterial).subscribe(success -> {
                if (success) {
                    log.info("[Redis 성공] 작업대상재가 성공적으로 저장되었습니다. ID : {}", targetMaterial.getId());
                } else {
                    log.warn("[Redis 실패] 작업대상재 저장에 실패했습니다. ID : {}", targetMaterial.getId());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("[역직렬화 실패] 카프카 메세지 바이트 변환에 실패했습니다. ", e);
        }
    }

    @Override
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    @Override
    public String getTopic() {
        return "control-targetMaterial-data";
    }

    @Override
    protected Mono<Boolean> saveData(TargetMaterialDTO.View data) {
        return targetCommandService.saveData(data);
    }
}
