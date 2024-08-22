package com.postco.control;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Control API", version = "1.0", description = "Control Service API"))
@EntityScan(basePackages = "com.postco.control.domain")
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
