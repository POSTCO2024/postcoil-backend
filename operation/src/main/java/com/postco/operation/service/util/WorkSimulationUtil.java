package com.postco.operation.service.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Slf4j
public class WorkSimulationUtil {
    private static final Random random = new Random();

    public static long convertToPresentationDuration(int expectedDurationMinutes) {
        long durationSeconds = expectedDurationMinutes * 2L;
        return Math.min(Math.max(durationSeconds, 10), 15);      // 계산이 10 ~ 15초 안에 나오도록 함
    }

    // 작업 종료 시뮬레이션
    public static Mono<Void> simulateWorkCompletion(long durationSeconds) {
        long delayMillis = (durationSeconds + random.nextInt(5) - 2) * 1000;
        log.info("작업 시뮬레이션 시작. 예상 소요 시간: {}초", delayMillis / 1000.0);

        return Mono.delay(Duration.ofMillis(delayMillis - 3000)) // 3초 전에 로그 찍기 시작
                .doOnNext(v -> log.info("작업 완료 3초 전..."))
                .then(Mono.delay(Duration.ofSeconds(3)))
                .doOnSuccess(v -> log.info("[작업 종료] 코일 작업 시뮬레이션 종료."))
                .then();
    }

    // 이송 종료 시뮬레이션
    public static Mono<Void> simulateDelivery() {
        long delayMillis = 10000; // 10초
        log.info("이송 시뮬레이션 시작. 예상 소요 시간: 10초");

        return Mono.delay(Duration.ofMillis(delayMillis - 1000))
                .doOnNext(v -> log.info("이송 완료 1초 전..."))
                .then(Mono.delay(Duration.ofSeconds(1)))
                .doOnSuccess(v -> log.info("[이송 종료] 코일 이송 시뮬레이션 종료."))
                .then();
    }
}
