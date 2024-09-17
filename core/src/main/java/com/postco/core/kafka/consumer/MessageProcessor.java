//package com.postco.core.kafka.consumer;
//
//import com.postco.core.redis.cqrs.command.CommandService;
//import com.postco.core.redis.cqrs.query.QueryService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Slf4j
//@RequiredArgsConstructor
//public class MessageProcessor<T> {
//
//    private final CommandService<T> commandService;
//    private final QueryService queryService;
//
//    // 메시지를 처리하고 저장하는 함수, getIdFunction으로 ID 추출 방식을 유연하게 처리
//    public void processMessages(List<T> messages, Function<T, String> getIdFunction, String keyPrefix, String processedSetKey) {
//        List<String> idList = messages.stream()
//                .map(getIdFunction)
//                .collect(Collectors.toList());
//
//        queryService.checkProcessedIdList(idList, processedSetKey)
//                .flatMapMany(processedMap -> Flux.fromIterable(messages)
//                        .filter(data -> {
//                            String id = getIdFunction.apply(data);
//                            boolean notProcessed = !processedMap.getOrDefault(id, false);
//                            if (!notProcessed) {
//                                log.info("[스킵] 이미 처리된 데이터: {}", id);
//                            }
//                            return notProcessed;
//                        })
//                        .flatMap(data -> saveData(getIdFunction.apply(data), data, keyPrefix))
//                )
//                .collectList()
//                .subscribe(
//                        results -> log.info("[Redis 성공] 새 메시지 {}개 처리 완료", results.stream().filter(Boolean::booleanValue).count()),
//                        error -> log.error("[Redis 실패] 메시지 처리 중 오류 발생", error),
//                        () -> log.info("[프로세스 종료] 메시지 처리 완료")
//                );
//    }
//
//    private Mono<Boolean> saveData(String id, T data, String keyPrefix) {
//        return commandService.saveData(id, data, keyPrefix)
//                .doOnSuccess(success -> log.info("[Redis 성공] 데이터 저장 성공: {}", id))
//                .doOnError(error -> log.error("[Redis 실패] 데이터 저장 중 오류 발생: {}", id, error));
//    }
//}