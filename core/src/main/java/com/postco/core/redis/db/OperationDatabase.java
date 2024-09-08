package com.postco.core.redis.db;

public class OperationDatabase implements RedisDatabase{
    // 0번
    @Override
    public int getMaterialDatabase() {
        return 0;
    }
    // 1번
    @Override
    public int getOrderDatabase() {
        return 1;
    }
}
