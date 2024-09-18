package com.postco.schedule;

import com.postco.core.config.RedisConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.modelmapper.ModelMapper;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Schedule API", version = "1.0", description = "Schedule Service API"))
@ComponentScan(basePackages = {"com.postco.schedule", "com.postco.core"})
@Import(RedisConfig.class)
public class ScheduleApp {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleApp.class, args);
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("schedule")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
