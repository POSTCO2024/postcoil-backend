package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.impl.MaterialCommandService;
import com.postco.cacheservice.service.impl.MaterialQueryService;
import com.postco.core.dto.MaterialDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MaterialConsumer extends GenericKafkaConsumer<MaterialDTO.View> {
    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public MaterialConsumer(ObjectMapper objectMapper,
                              MaterialQueryService queryService,
                              MaterialCommandService commandService) {
        super(objectMapper, queryService, commandService);
    }

    @Override
    protected MaterialDTO.View deserializeMessage(String message) {
        try {
            return objectMapper.readValue(message, MaterialDTO.View.class);
        } catch (JsonProcessingException e) {
            log.error("[Kafka 실패] 재료 메시지 역직렬화 중 오류 발생: {}", message, e);
            return null;
        }
    }

    @Override
    protected String getDataId(MaterialDTO.View data) {
        return String.valueOf(data.getMaterialId());
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
    public String getGroupId() {
        return groupId;
    }
}