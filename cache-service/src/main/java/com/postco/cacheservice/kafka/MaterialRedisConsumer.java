package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.MaterialDataService;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialRedisConsumer extends GenericKafkaConsumer<MaterialDTO.View> {
    private final MaterialDataService materialDataService;
    private final ObjectMapper objectMapper;

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    @Override
    public void consumeMessage(String message) {
        log.info("Kafka 활성화 여부 : {}", kafkaEnabled);
        if (!isKafkaEnabled()) {
            log.warn("[Kafka OFF] 카프카가 비활성화 되었습니다. 메세지를 수신받지 않습니다.");
            return;
        }

        try {
            MaterialDTO.View data = objectMapper.readValue(message, MaterialDTO.View.class);
            log.info("[Kafka ON] 재료 데이터 수신 : {}", data);

            saveData(data)
                    .doOnSuccess(result -> {
                        if (result) {
                            log.info("[Redis 저장 성공] 재료 데이터를 성공적으로 저장했습니다. 재료 ID: {}", data.getId());
                        } else {
                            log.warn("[Redis 저장 실패] 재료 데이터 저장에 실패했습니다. 재료 ID: {}", data.getId());
                        }
                    })
                    .doOnError(e -> log.error("[Error] Redis 에 저장하는 데 에러 발생. 재료 ID: {}", data.getId(), e))
                    .subscribe();
        } catch (JsonProcessingException e) {
            log.error("[데이터 처리 실패] Kafka 데이터 처리에 실패 하였습니다.", e);
            throw new RuntimeException("Failed to serialize data", e);
        }
    }

    @Override
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    @Override
    public String getTopic() {
        return "operation-material-data";
    }

    @Override
    protected Mono<Boolean> saveData(MaterialDTO.View data) {
        return materialDataService.saveMaterials(data);
    }


}