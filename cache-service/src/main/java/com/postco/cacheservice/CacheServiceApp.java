package com.postco.cacheservice;

import com.postco.core.config.RedisConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class CacheServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(CacheServiceApp.class, args);
    }

}
