package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.impl.OrderCommandService;
import com.postco.cacheservice.service.impl.OrderQueryService;
import com.postco.core.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer extends GenericKafkaConsumer<OrderDTO.View> {
    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public OrderConsumer(ObjectMapper objectMapper,
                            OrderQueryService queryService,
                            OrderCommandService commandService) {
        super(objectMapper, queryService, commandService);
    }

    @Override
    protected OrderDTO.View deserializeMessage(String message) {
        try {
            return objectMapper.readValue(message, OrderDTO.View.class);
        } catch (JsonProcessingException e) {
            log.error("[Kafka 실패] 주문 메시지 역직렬화 중 오류 발생: {}", message, e);
            return null;
        }
    }

    @Override
    protected String getDataId(OrderDTO.View data) {
        return String.valueOf(data.getId());
    }

    @Override
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    @Override
    public String getTopic() {
        return "operation-order-data";
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

}
