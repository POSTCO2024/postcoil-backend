package com.postco.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.kafka.KafkaMessageStrategy;
import com.postco.core.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;
    private final List<KafkaMessageStrategy<?>> messageStrategies;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");

        // 처리량 관련 설정
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);

        // 연결 관련 설정
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 멱등성 설정 : default 는 1( at least once)
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // 전송 타임아웃 설정 (기본값은 2분 = 120,000ms)
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "120000");
        // 동시 처리 가능 횟수 : 5개
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    @ConditionalOnProperty(name = "feature-flags.kafka.enabled", havingValue = "true")
    public KafkaProducer genericProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        return new KafkaProducer(kafkaTemplate, objectMapper);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);  // 파티션 개수와 동일하게 설정
        factory.setCommonErrorHandler(dlqErrorHandler());
        return factory;
    }

    @Bean
    public CommonErrorHandler dlqErrorHandler() {
        DeadLetterPublishingRecoverer dlqRecover = new DeadLetterPublishingRecoverer(kafkaTemplate(),
                (record, e) -> {
                    log.error("topic: {}, cause: {}, value: {}", record.topic(), e.getMessage(), record.value());
                    return new TopicPartition(record.topic() + ".dlc", record.partition());
                });

        return new DefaultErrorHandler(dlqRecover, new FixedBackOff(0L, 2L)); // 2번 재시도 후 DLQ로 전송
    }


//    @Bean
//    public KafkaConsumerFactory kafkaConsumerFactory() {
//        return new KafkaConsumerFactory(messageStrategies, objectMapper, kafkaListenerContainerFactory());
//    }
//
//    // 동적 컨슈머 생성을 위한 메서드
//    @Bean
//    public List<GenericKafkaConsumer<?>> kafkaConsumers() {
//        return messageStrategies.stream()
//                .map(strategy -> kafkaConsumerFactory().createConsumer(strategy.getDataType()))
//                .collect(Collectors.toList());
//    }
}