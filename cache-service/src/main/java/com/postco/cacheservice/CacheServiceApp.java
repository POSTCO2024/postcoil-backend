package com.postco.cacheservice;

import com.postco.core.config.RedisConfig;
import com.postco.core.config.KafkaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@Import({RedisConfig.class, KafkaConfig.class})
@EnableDiscoveryClient
public class CacheServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(CacheServiceApp.class, args);
    }

}