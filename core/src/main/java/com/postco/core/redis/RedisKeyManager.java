package com.postco.core.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis.prefix")
@Getter
@Setter
public class RedisKeyManager {
    private String targetPrefix;
    private String materialPrefix;
    private String orderPrefix;

    public String getTargetKey(Long id) {
        return targetPrefix + id;
    }

    public String getMaterialKey(Long id) {
        return materialPrefix + id;
    }

    public String getOrderKey(Long id) {
        return orderPrefix + id;
    }
}
