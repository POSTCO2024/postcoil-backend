package com.postco.control;

import com.postco.control.service.ExtractionFilter;
import com.postco.control.service.RedisService;
import com.postco.control.service.impl.ExtractionFilterService;
import com.postco.control.service.impl.TargetMaterialServiceImpl;
import com.postco.core.config.redis.RedisConfig;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.TargetMaterialDTO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import reactor.core.publisher.Mono;

import java.util.List;

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
    public ApplicationRunner targetMaterialProcessor(TargetMaterialServiceImpl targetMaterialService) {
        return args -> {
            log.info("Starting to process target materials...");

            String processCode = "1PCM"; // 예시 프로세스 코드, 실제 사용할 코드로 변경해야 합니다.

            Mono<List<TargetMaterialDTO.View>> resultMono = targetMaterialService.processTargetMaterials(processCode);

            resultMono.subscribe(
                    targetMaterials -> {
                        log.info("Processed {} target materials", targetMaterials.size());
                        targetMaterials.forEach(material -> log.info("Processed Target Material: {}", material));
                    },
                    error -> log.error("Error processing target materials", error),
                    () -> log.info("Finished processing target materials")
            );
        };
    }
}
