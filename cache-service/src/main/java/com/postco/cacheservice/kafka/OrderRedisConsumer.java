package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.cacheservice.service.MaterialDataService;
import com.postco.cacheservice.service.OrderDataService;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRedisConsumer extends GenericKafkaConsumer<OrderDTO.View> {
    private final OrderDataService orderDataService;
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
            OrderDTO.View data = objectMapper.readValue(message, OrderDTO.View.class);
            log.info("[Kafka ON] 주문 데이터 수신 : {}", data);

            saveData(data)
                    .doOnSuccess(result -> {
                        if (result) {
                            log.info("[Redis 저장 성공] 주문 데이터를 성공적으로 저장했습니다. 재료 ID: {}", data.getId());
                        } else {
                            log.warn("[Redis 저장 실패] 주문 데이터 저장에 실패했습니다. 재료 ID: {}", data.getId());
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
        return "operation-order-data";
    }

    @Override
    protected Mono<Boolean> saveData(OrderDTO.View data) {
        return orderDataService.saveOrder(data);
    }
}
