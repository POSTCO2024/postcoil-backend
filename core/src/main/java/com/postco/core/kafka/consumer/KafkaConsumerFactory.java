package com.postco.core.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.kafka.KafkaMessageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import java.util.List;
@RequiredArgsConstructor
public class KafkaConsumerFactory {
    private final List<KafkaMessageStrategy<?>> messageStrategies;
    private final ObjectMapper objectMapper;
    private final ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    @SuppressWarnings("unchecked")
    public <T> GenericKafkaConsumer<T> createConsumer(Class<T> dataType) {
        KafkaMessageStrategy<T> strategy = (KafkaMessageStrategy<T>) messageStrategies.stream()
                .filter(s -> s.getDataType().equals(dataType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for type: " + dataType));

        return new GenericKafkaConsumer<>(strategy, objectMapper, kafkaListenerContainerFactory);
    }
}
