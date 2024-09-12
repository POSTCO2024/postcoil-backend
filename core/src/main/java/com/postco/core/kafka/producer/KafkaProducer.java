package com.postco.core.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.exception.KafkaSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public <T> void sendData(String topic, T data) {
        if (!kafkaEnabled) {
            log.warn("[Kafka OFF] 카프카가 비활성화 되었습니다. 데이터를 보내지 않습니다.");
            return;
        }

        try {
            String jsonData = objectMapper.writeValueAsString(data);
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, jsonData);

            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("[Kafka 전송 성공] 토픽 {}로 데이터를 전송했습니다. 오프셋: {}", topic, result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("[Kafka 전송 실패] 토픽 {}로 데이터 전송을 실패했습니다. 예외: {}", topic, ex.getMessage());
                    handleFailure(topic, jsonData, ex);
                }
            });
        } catch (Exception e) {
            log.error("[Kafka 직렬화 실패] 토픽 {}에 대한 데이터 직렬화를 실패했습니다.", topic, e);
            handleSerializationFailure(topic, data, e);
        }
    }

    private void handleFailure(String topic, String jsonData, Throwable ex) {
        RetryMessage retryMessage = new RetryMessage(topic, jsonData);
        retryQueue.offer(retryMessage);
        log.warn("[Kafka 재시도 큐] 실패한 메시지를 재시도 큐에 추가했습니다. 토픽: {}, 데이터: {}", topic, jsonData);
    }

    private void handleSerializationFailure(String topic, Object data, Exception e) {
        log.error("[Kafka 직렬화 실패 처리] 토픽 {}에 대한 데이터 직렬화 실패를 처리합니다.", topic, e);
        // 직렬화 실패에 대한 처리 로직 추가 가능
    }

    @Scheduled(fixedDelayString = "${kafka.retry.delay.ms:60000}")
    public void retryFailedMessages() {
        RetryMessage retryMessage;
        while ((retryMessage = retryQueue.poll()) != null) {
            try {
                kafkaTemplate.send(retryMessage.getTopic(), retryMessage.getData());
                log.info("[Kafka 재시도 성공] 토픽 {}로 데이터를 재전송했습니다.", retryMessage.getTopic());
            } catch (Exception e) {
                log.error("[Kafka 재시도 실패] 토픽 {}로 데이터 재전송을 실패했습니다. 예외: {}", retryMessage.getTopic(), e.getMessage());
                retryQueue.offer(retryMessage); // 재시도 실패 시 다시 큐에 넣음
            }
        }
    }
}