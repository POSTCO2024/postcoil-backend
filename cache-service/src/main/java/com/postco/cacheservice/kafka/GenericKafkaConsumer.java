package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.AbstractRedisCommandService;
import com.postco.core.redis.AbstractRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class GenericKafkaConsumer<T> {

    protected final ObjectMapper objectMapper;
    protected final AbstractRedisQueryService<T> queryService;
    protected final AbstractRedisCommandService<T> commandService;

    @KafkaListener(topics = "#{__listener.topic}", groupId = "#{__listener.groupId}", autoStartup = "#{__listener.kafkaEnabled}", batch = "true")
    public void consumeMessages(List<String> messages) {
        List<T> dataList = messages.stream()
                .map(this::deserializeMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<String> idList = dataList.stream()
                .map(this::getDataId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        queryService.checkProcessedIdList(idList)
                .flatMapMany(processedMap -> Flux.fromIterable(dataList)
                        .filter(data -> {
                            String id = getDataId(data);
                            boolean notProcessed = id != null && !processedMap.getOrDefault(id, false);
                            if (!notProcessed) {
                                log.info("[스킵] 이미 처리된 데이터: {}", id);
                            }
                            return notProcessed;
                        })
                        .flatMap(this::saveData)
                )
                .collectList()
                .subscribe(
                        results -> log.info("[Redis 성공] 새 메시지 {}개 처리 완료", results.stream().filter(Boolean::booleanValue).count()),
                        error -> log.error("[Redis 실패] 메시지 배치 처리 중 오류 발생", error),
                        () -> log.info("[프로세스 종료] 메시지 처리 완료")
                );
    }

    private Mono<Boolean> saveData(T data) {
        String id = getDataId(data);
        return commandService.saveData(data)
                .doOnSuccess(success -> log.info("[Redis 성공] 데이터 저장 성공: {}", id)) // 성공 시 메시지 출력
                .doOnError(error -> log.error("[Redis 실패] 데이터 저장 중 오류 발생: {}", id, error)); // 오류 시 메시지 출력
    }

    protected abstract T deserializeMessage(String message);
    protected abstract String getDataId(T data);
    public abstract boolean isKafkaEnabled();
    public abstract String getTopic();
    public abstract String getGroupId();
}