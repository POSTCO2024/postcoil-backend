package com.postco.control;

import com.postco.control.service.impl.TargetMaterialServiceImpl;
import com.postco.core.config.RedisConfig;
import com.postco.core.dto.TargetMaterialDTO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Control API", version = "1.0", description = "Control Service API"))
@EntityScan(basePackages = "com.postco.control.domain")
@ComponentScan(basePackages = {"com.postco.control", "com.postco.core"})
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
            log.info("[작업대상재 Start] 작업대상재 추출 프로세스를 시작합니다. 진행중 ...");

            List<String> processCodes = List.of("1PCM", "2PCM", "1CAL", "2CAL", "1EGL", "2EGL", "1CGL");

            Mono<List<TargetMaterialDTO.View>> resultMono = Mono.just(processCodes)
                    .flatMapIterable(codes -> codes)
                    .flatMap(targetMaterialService::processTargetMaterials)
                    .collectList()
                    .map(lists -> lists.stream().flatMap(List::stream).collect(Collectors.toList()));

            resultMono.subscribe(
                    targetMaterials -> {
                        log.info("[작업 성공] 모든 공정에서 {}개의 작업 대상 재료가 처리되었습니다", targetMaterials.size());
                        targetMaterials.forEach(material -> log.info("처리된 작업 대상 재료: {}", material));
                    },
                    error -> log.error("[작업 오류] 작업 대상 재료 처리 중 오류 발생", error),
                    () -> log.info("[작업대상재 Finish] 작업 대상재 프로세스 종료")
            );
        };
    }
}
