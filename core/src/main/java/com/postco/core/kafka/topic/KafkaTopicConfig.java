package com.postco.core.kafka.topic;

import lombok.Data;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaTopicConfig {
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic getMaterialTopic() {
        return TopicBuilder.name("operation-material-data")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic changeMaterialTopic() {
        return TopicBuilder.name("change-data.material")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic getOrderTopic() {
        return TopicBuilder.name("operation-order-data")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic getTargetMaterialTopic() {
        return TopicBuilder.name("control-targetMaterial-data")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic getScheduleResultTopic() {
        return TopicBuilder.name("schedule-confirm-data")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic getWebsocketTopic() {
        return TopicBuilder.name("operation-websocket-data")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
