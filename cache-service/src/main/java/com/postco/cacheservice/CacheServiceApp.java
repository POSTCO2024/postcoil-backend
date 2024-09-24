package com.postco.cacheservice;

import com.postco.core.config.RedisConfig;
import com.postco.core.config.KafkaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@Import({RedisConfig.class, KafkaConfig.class})
public class CacheServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(CacheServiceApp.class, args);
    }

}

/*
To 최효준
효준 오랜만이다
잘 지내냐
개발 야무지게 하고 있구나~
역시 믿고 보는 최효준
우리 팀으로 와라
담배 좀 줄이고
어?

화이팅이다 아자아자
 */