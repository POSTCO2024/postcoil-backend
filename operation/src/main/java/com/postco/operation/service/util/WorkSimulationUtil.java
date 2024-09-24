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
        return Math.min(Math.max(durationSeconds, 20), 25);
    }

    public static Mono<Void> simulateWorkCompletion(long durationSeconds) {
        long delayMillis = (durationSeconds + random.nextInt(6) - 3) * 1000;
        log.info("작업 시뮬레이션 시작. 예상 소요 시간: {}초", delayMillis / 1000.0);

        return Mono.delay(Duration.ofMillis(delayMillis))
                .flatMap(v -> logProgress(delayMillis))
                .then();
    }

    private static Mono<Void> logProgress(long totalMillis) {
        return Mono.defer(() -> {
            long startTime = System.currentTimeMillis();
            return Mono.delay(Duration.ofSeconds(1))
                    .repeat()
                    .takeUntil(v -> System.currentTimeMillis() - startTime >= totalMillis - 3000)
                    .doOnNext(v -> logRemainingTime(startTime, totalMillis))
                    .then(Mono.delay(Duration.ofSeconds(3)))
                    .doOnSuccess(v -> log.info("[작업 종료] 코일 작업 시뮬레이션 종료."))
                    .then();
        });
    }

    private static void logRemainingTime(long startTime, long totalMillis) {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        long remainingMillis = totalMillis - elapsedMillis;
        if (remainingMillis > 0) {
            log.debug("남은 작업 시간: {}초", remainingMillis / 1000.0);
        }
    }
}
