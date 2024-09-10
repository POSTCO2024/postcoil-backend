package com.postco.operation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSendService {

    private final KafkaMessageService kafkaMessageService;

    public void sendAllData() {
        log.info("[Kafka 전송] 재료 및 주문 데이터 전송 시작...");
        kafkaMessageService.sendAllMaterials();
        kafkaMessageService.sendOrders();
       log.info("[Kafka 완료] 카프카 데이터 전송 완료");
    }
}
