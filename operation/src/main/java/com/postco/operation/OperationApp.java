package com.postco.operation;

import com.postco.core.config.AuditorAwareImpl;
import com.postco.operation.service.DataInitService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.AuditorAware;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "Operation API", version = "1.0", description = "Operation Service API"))
@EntityScan(basePackages = "com.postco.operation.domain.entity")
@ComponentScan(basePackages = {"com.postco.core", "com.postco.operation"})
//@EnableJpaAuditing
public class OperationApp {

    public static void main(String[] args) {
        SpringApplication.run(OperationApp.class, args);
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("operation")
                .pathsToMatch("/api/**")
                .build();
    }
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    @Bean
    public CommandLineRunner initializeDataOnStartup(DataInitService dataInitService) {
        return args -> dataInitService.initializeAllData().block();
    }
}
