package com.postco.control;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ControlApp {

    public static void main(String[] args) {
        SpringApplication.run(ControlApp.class, args);
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("control")
                .pathsToMatch("/api/**")
                .build();
    }
}
