package com.postco.core.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.exception.KafkaSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ConcurrentLinkedQueue;


@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentLinkedQueue<RetryMessage> retryQueue = new ConcurrentLinkedQueue<>();

    @Value("${feature-flags.kafka.enabled}")
    private boolean kafkaEnabled;

    public <T> void sendData(String topic, String key, T data) {
        if (!kafkaEnabled) {
            log.warn("[Kafka OFF] 카프카가 비활성화 되었습니다. 데이터를 보내지 않습니다.");
            return;
        }

        try {
            String jsonData = objectMapper.writeValueAsString(data);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, jsonData);

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(record);

            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("[Kafka 전송 성공] 토픽 {}로 데이터를 전송했습니다. 키: {}, 오프셋: {}, json: {}", topic, key, result.getRecordMetadata().offset(), jsonData);
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("[Kafka 전송 실패] 토픽 {}로 데이터 전송을 실패했습니다. 키: {}, 예외: {}", topic, key, ex.getMessage());
                    handleFailure(topic, key, jsonData, ex);
                }
            });
        } catch (Exception e) {
            log.error("[Kafka 직렬화 실패] 토픽 {}에 대한 데이터 직렬화를 실패했습니다. 키: {}", topic, key, e);
            handleSerializationFailure(topic, key, data, e);
        }
    }

    private void handleFailure(String topic, String key, String jsonData, Throwable ex) {
        RetryMessage retryMessage = new RetryMessage(topic, key, jsonData);
        retryQueue.offer(retryMessage);
        log.warn("[Kafka 재시도 큐] 실패한 메시지를 재시도 큐에 추가했습니다. 토픽: {}, 키: {}, 데이터: {}", topic, key, jsonData);
    }

    private void handleSerializationFailure(String topic, String key, Object data, Exception e) {
        log.error("[Kafka 직렬화 실패 처리] 토픽 {}에 대한 데이터 직렬화 실패를 처리합니다. 키: {}", topic, key, e);
        // 직렬화 실패에 대한 처리 로직 추가 가능
    }

    @Scheduled(fixedDelayString = "${kafka.retry.delay.ms:60000}")
    public void retryFailedMessages() {
        RetryMessage retryMessage;
        while ((retryMessage = retryQueue.poll()) != null) {
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(retryMessage.getTopic(), retryMessage.getKey(), retryMessage.getData());
                kafkaTemplate.send(record);
                log.info("[Kafka 재시도 성공] 토픽 {}로 데이터를 재전송했습니다. 키: {}", retryMessage.getTopic(), retryMessage.getKey());
            } catch (Exception e) {
                log.error("[Kafka 재시도 실패] 토픽 {}로 데이터 재전송을 실패했습니다. 키: {}, 예외: {}", retryMessage.getTopic(), retryMessage.getKey(), e.getMessage());
                retryQueue.offer(retryMessage); // 재시도 실패 시 다시 큐에 넣음
            }
        }
    }
}