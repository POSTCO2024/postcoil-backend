package com.postco.core.redis.cqrs.query;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface QueryService {

    /**
     * 단일 Redis 키에 대한 데이터를 비동기로 가져오는 메서드.
     * @param key Redis 키 (ex. material:1)
     * @param entityClass 조회할 엔티티 클래스 타입
     */
    <T> Mono<T> fetchByKeyFromRedis(String key, Class<T> entityClass);

    /**
     * 단일 프리픽스 내의 모든 데이터를 비동기로 가져오는 메서드.
     * @param prefix Redis 프리픽스 (ex. material:)
     * @param entityClass 조회할 엔티티 클래스 타입
     */
    <T> Flux<T> fetchAllBySinglePrefix(String prefix, Class<T> entityClass);

    /**
     * 여러 프리픽스에 해당하는 데이터를 비동기로 가져오는 메서드.
     * @param prefixes 여러 Redis 프리픽스 리스트
     * @param entityClass 조회할 엔티티 클래스 타입
     */
    <T> Flux<T> fetchAllByMultiplePrefixes(List<String> prefixes, Class<T> entityClass);

    /**
     * 특정 프리픽스 내에서 지정된 키들의 데이터를 비동기로 가져오는 메서드.
     * @param prefix Redis 프리픽스
     * @param keys 가져올 키 리스트
     * @param entityClass 조회할 엔티티 클래스 타입
     */
    <T> Flux<T> fetchByKeysInSinglePrefix(String prefix, List<String> keys, Class<T> entityClass);

    /**
     * 여러 프리픽스와 그에 해당하는 특정 키들의 데이터를 비동기로 가져오는 메서드.
     * @param prefixKeysMap 프리픽스와 그에 해당하는 키들의 Map
     * @param entityClass 조회할 엔티티 클래스 타입
     */
    <T> Flux<T> fetchByKeysInMultiplePrefixes(Map<String, List<String>> prefixKeysMap, Class<T> entityClass);

    /**
     * 처리된 ID 리스트를 확인하는 메서드.
     * @param idList 확인할 ID 리스트
     * @param processedSetKey 처리된 ID를 저장하는 Redis Set 키
     */
    Mono<Map<String, Boolean>> checkProcessedIdList(List<String> idList, String processedSetKey);
}
