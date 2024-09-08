package com.postco.control;

import com.postco.control.service.RedisService;
import com.postco.core.config.redis.RedisConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import reactor.core.publisher.Mono;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Control API", version = "1.0", description = "Control Service API"))
@EntityScan(basePackages = "com.postco.control.domain")
@Import(RedisConfig.class)
@EnableJpaAuditing
@Slf4j
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

    @Bean
    public CommandLineRunner redisDataLoader(RedisService redisService) {
        return args -> {
            log.info("Loading all material data from Redis...");

            Mono<Void> loadData = redisService.getAllMaterialsFromRedis()
                    .doOnNext(materials -> {
                        log.info("Loaded {} materials from Redis", materials.size());
                        materials.forEach(material ->
                                log.info("Material: {}", material.toString())
                        );
                    })
                    .then();

            // Block until data is loaded
            loadData.block();

            log.info("Finished loading all material data from Redis");
        };
    }
}
