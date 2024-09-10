package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.impl.MaterialCommandService;
import com.postco.core.dto.MaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialConsumer extends GenericKafkaConsumer<MaterialDTO.View> {
    private final MaterialCommandService materialCommandService; // Redis 저장 서비스
    private final ObjectMapper objectMapper;

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    @Override
    public void consumeMessage(String message) {
        try {
            MaterialDTO.View material = objectMapper.readValue(message, MaterialDTO.View.class);
            log.info("Received materials data from Kafka: {}", material);

            saveData(material).subscribe(success -> {
                if (success) {
                    log.info("Material successfully saved in Redis: {}", material.getId());
                } else {
                    log.warn("Failed to save material in Redis: {}", material.getId());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize Kafka message", e);
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
    protected Mono<Boolean> saveData(MaterialDTO.View material) {
        return materialCommandService.saveData(material);
    }
}

//@Component
//@RequiredArgsConstructor
//public class MaterialKafkaConsumer implements KafkaMessageStrategy<MaterialDTO.View> {
//    private final CommandService<MaterialDTO.View> commandService;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public Class<MaterialDTO.View> getDataType() {
//        return MaterialDTO.View.class;
//    }
//
//    @Override
//    public Mono<Boolean> processMessage(String message) {
//        try {
//            MaterialDTO.View data = objectMapper.readValue(message, MaterialDTO.View.class);
//            return commandService.saveData(data);
//        } catch (Exception e) {
//            return Mono.error(new RuntimeException("Failed to process message", e));
//        }
//    }
//
//    @Override
//    public String getTopic() {
//        return "operation-material-data";
//    }
//}