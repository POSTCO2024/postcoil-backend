package com.postco.core.redis.db;


/**
 * Redis 는 0번부터 15번까지 총 16개의 database instance 를 가집니다.
 * 엔티티별로 저장할 위치를 선택 및 관리하는 클래스 입니다.
 * 서비스 별로 분리하고, 메서드로 각 엔티티를 구별합니다.
 */
public interface RedisDatabase {
    int getMaterialDatabase();
    int getOrderDatabase();
}
