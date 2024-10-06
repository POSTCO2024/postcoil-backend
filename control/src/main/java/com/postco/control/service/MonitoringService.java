package com.postco.control.service;

import com.postco.control.service.impl.redis.ControlRedisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringService {
    private final ControlRedisQueryService controlRedisQueryService;


    /**
     * 레디스의 모든 재료 정보를 가져와서, 각 저장위치의 동에따라 구룹핑하여 카운트
     *
     * @return
     */
    public Mono<Map<String, Long>> getMaterialsWithLocation() {
//        controlRedisQueryService.getAllMaterialsFromRedis().subscribe(list -> {
//            list.forEach(System.out::println);
//        });
        return controlRedisQueryService.getAllMaterialsFromRedis()
                .map(list -> list.stream()
                                .collect(Collectors.groupingBy(
//                                material -> Integer.parseInt(String.valueOf(material.getStorageLoc()).substring(0, 1)),
//                                Collectors.counting()
                                        material -> material.getYard(),
                                        Collectors.counting()
                                ))
                );

    }
}
