package com.postco.core.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;

import javax.annotation.PostConstruct;
@Slf4j
@RequiredArgsConstructor
public class GenericKafkaConsumer<T> {
    private final KafkaMessageStrategy<T> strategy;
    private final ObjectMapper objectMapper;
    private final ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    @PostConstruct
    public void init() {
        ConcurrentMessageListenerContainer<String, String> container =
                kafkaListenerContainerFactory.createContainer(strategy.getTopic());

        container.getContainerProperties().setMessageListener((MessageListener<String, String>) record -> {
            strategy.processMessage(record.value())
                    .subscribe(
                            result -> log.info("Message processed successfully: {}", result),
                            error -> log.error("Error processing message: ", error)
                    );
        });

        container.start();
    }
}
