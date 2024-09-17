//package com.postco.core.redis.cqrs.command;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.ReactiveHashOperations;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.lang.reflect.Method;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * 제네릭 Redis Command 서비스 클래스입니다.
// * 다양한 엔티티와 prefix를 동적으로 처리하며, 데이터를 저장, 수정, 삭제합니다.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class GenericRedisCommandService<T> implements CommandService<T> {
//    private final ReactiveRedisTemplate<String, Object> redisTemplate;
//    private final ObjectMapper objectMapper;
//
//    private final String prefix;  // 외부에서 전달받은 prefix
//    private final Class<T> entityClass; // 외부에서 전달받은 엔티티 클래스
//
//    /**
//     * 데이터를 Redis 에 저장하는 메서드.
//     * @param data 저장할 데이터
//     * @return 저장 성공 여부
//     */
//    @Override
//    public Mono<Boolean> saveData(T data) {
//        ReactiveHashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
//        Map<String, Object> map = objectMapper.convertValue(data, new TypeReference<>() {});
//        Map<String, String> dataMap = map.entrySet().stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        entry -> String.valueOf(entry.getValue())
//                ));
//
//        String id = getIdFromData(data);
//        String key = prefix + id;  // 동적 prefix 사용
//
//        return hashOperations.putAll(key, dataMap)
//                .then(addProcessedId(id))  // 처리된 ID 추가
//                .thenReturn(true);
//    }
//
//    /**
//     * 처리된 ID를 Redis Set에 추가하는 메서드.
//     * @param id 처리된 ID
//     * @return 추가 성공 여부
//     */
//    @Override
//    public Mono<Boolean> addProcessedId(String id) {
//        String processedSetKey = getProcessedIdsKey();  // 처리된 ID 저장용 키
//        return redisTemplate.opsForSet().add(processedSetKey, id)
//                .doOnSuccess(result -> log.info("[Redis 성공] 처리된 ID 저장: {}", id))
//                .doOnError(error -> log.error("[Redis 실패] 처리된 ID 저장 중 오류 발생: {}", id, error))
//                .hasElement();
//    }
//
//    /**
//     * Redis에 저장된 데이터를 업데이트하는 메서드.
//     * @param id 업데이트할 데이터의 ID
//     * @param data 새로운 데이터
//     * @return 업데이트 성공 여부
//     */
//    @Override
//    public Mono<Boolean> updateData(String id, T data) {
//        return saveData(data);  // 업데이트는 데이터 저장과 동일하게 처리
//    }
//
//    /**
//     * Redis에서 데이터를 삭제하는 메서드.
//     * @param id 삭제할 데이터의 ID
//     * @return 삭제 성공 여부
//     */
//    @Override
//    public Mono<Boolean> deleteData(String id) {
//        return redisTemplate.delete(prefix + id).map(result -> result > 0);
//    }
//
//    /**
//     * 데이터에서 ID를 추출하는 메서드.
//     * @param data 저장할 데이터
//     * @return 데이터에서 추출한 ID
//     */
//    private String getIdFromData(T data) {
//        try {
//            Method getIdMethod = entityClass.getMethod("getId");  // 리플렉션으로 ID 추출 메서드 호출
//            Long id = (Long) getIdMethod.invoke(data);
//            return String.valueOf(id);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to get ID from data", e);
//        }
//    }
//
//    /**
//     * 처리된 ID를 저장할 Redis Set 의 키를 반환하는 메서드.
//     * @return 처리된 ID용 Redis 키
//     */
//    private String getProcessedIdsKey() {
//        return prefix + "processed_idSet";
//    }
//}