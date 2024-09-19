package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.impl.ScheduleResultCommandService;
import com.postco.cacheservice.service.impl.ScheduleResultQueryService;
import com.postco.cacheservice.service.impl.TargetMaterialCommandService;
import com.postco.cacheservice.service.impl.TargetMaterialQueryService;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleConsumer extends GenericKafkaConsumer<ScheduleResultDTO.View>{
    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public ScheduleConsumer(ObjectMapper objectMapper,
                                  ScheduleResultQueryService queryService,
                                  ScheduleResultCommandService commandService) {
        super(objectMapper, queryService, commandService);
    }

    @Override
    protected ScheduleResultDTO.View deserializeMessage(String message) {
        try {
            return objectMapper.readValue(message, ScheduleResultDTO.View.class);
        } catch (JsonProcessingException e) {
            log.error("[Kafka 실패] 스케쥴 결과 메시지 역직렬화 중 오류 발생: {}", message, e);
            return null;
        }
    }

    @Override
    protected String getDataId(ScheduleResultDTO.View data) {
        return String.valueOf(data.getId());
    }

    @Override
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    @Override
    public String getTopic() {
        return "schedule-confirm-data";
    }

    @Override
    public String getGroupId() {
        return groupId;
    }
}
