package com.postco.core.redis.cqrs.command;

import reactor.core.publisher.Mono;


public interface CommandService<T> {
    /**
     * 데이터를 저장하는 메서드.
     * @param data 저장할 데이터
     * @return 저장 성공 여부
     */
    Mono<Boolean> saveData(T data);

    /**
     * 처리된 ID를 Set에 추가하는 메서드.
     * @param id 처리된 ID
     * @return 추가 성공 여부
     */
    Mono<Boolean> addProcessedId(String id);

    /**
     * 데이터를 업데이트하는 메서드.
     * @param id 업데이트할 데이터의 ID
     * @param data 새로운 데이터
     * @return 업데이트 성공 여부
     */
    Mono<Boolean> updateData(String id, T data);

    /**
     * 데이터를 삭제하는 메서드.
     * @param id 삭제할 데이터의 ID
     * @return 삭제 성공 여부
     */
    Mono<Boolean> deleteData(String id);
}