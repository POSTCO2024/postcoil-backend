package com.postco.cacheservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.redis.AbstractRedisCommandService;
import com.postco.core.redis.AbstractRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class GenericKafkaConsumer<T> {

    protected final ObjectMapper objectMapper;
    protected final AbstractRedisQueryService<T> queryService;
    protected final AbstractRedisCommandService<T> commandService;

    @KafkaListener(topics = "#{__listener.topic}", groupId = "#{__listener.groupId}", autoStartup = "#{__listener.kafkaEnabled}", batch = "true")
    public void consumeMessages(List<ConsumerRecord<String, String>> records) {
        Map<String, T> dataMap = records.stream()
                .map(record -> {
                    T data = deserializeMessage(record.value());
                    return data != null ? Map.entry(record.key(), data) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v2  // 중복 키의 경우 최신 값을 유지
                ));

        List<String> idList = dataMap.values().stream()
                .map(this::getDataId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        queryService.checkProcessedIdList(idList)
                .flatMapMany(processedMap -> Flux.fromIterable(dataMap.entrySet())
                        .filter(entry -> {
                            String id = getDataId(entry.getValue());
                            boolean notProcessed = id != null && !processedMap.getOrDefault(id, false);
                            if (!notProcessed) {
                                log.info("[스킵] 이미 처리된 데이터: {}", id);
                            }
                            return notProcessed;
                        })
                        .flatMap(entry -> saveData(entry.getKey(), entry.getValue()))
                )
                .collectList()
                .subscribe(
                        results -> log.info("[Redis 성공] 새 메시지 {}개 처리 완료", results.stream().filter(Boolean::booleanValue).count()),
                        error -> log.error("[Redis 실패] 메시지 배치 처리 중 오류 발생", error),
                        () -> log.info("[프로세스 종료] 메시지 처리 완료")
                );
    }

    private Mono<Boolean> saveData(String key, T data) {
        String id = getDataId(data);
        return commandService.saveData(data)
                .doOnSuccess(success -> log.info("[Redis 성공] 데이터 저장 성공: 키 = {}, ID = {}", key, id))
                .doOnError(error -> log.error("[Redis 실패] 데이터 저장 중 오류 발생: 키 = {}, ID = {}", key, id, error));
    }

    protected abstract T deserializeMessage(String message);
    protected abstract String getDataId(T data);
    public abstract boolean isKafkaEnabled();
    public abstract String getTopic();
    public abstract String getGroupId();
}