package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.RedisDataContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashBoardOrderService {
    private final ControlRedisQueryService controlRedisQueryService;
    private final TargetMaterialRepository targetMaterialRepository;


    // 품종 비율 결과 
    public Mono<Map<String, Long>> getCoilTypesByCurrProc(String currProc) {
        return getNormalMaterialsAndOrdersByProc(currProc)
                .map(container -> countCoilTypes(container.getMaterials()));
    }

    // 고객사 비율 결과
    public Mono<Map<String, Long>> getCustomerCountByProc(String currProc) {
        return getNormalMaterialsAndOrdersByProc(currProc)
                .map(this::countCustomers);
    }

    // 정상재인 것만 조회해서 해당하는 공정의 재료 데이터와 주문 데이터 가져오는 메서드
    private Mono<RedisDataContainer> getNormalMaterialsAndOrdersByProc(String currProc) {
        return Mono.fromCallable(() -> targetMaterialRepository.findByIsError("N"))   // N 인 정상재만 찾기
                .flatMap(normalMaterials -> {
                    Set<Long> normalMaterialIds = normalMaterials.stream()
                            .map(TargetMaterial::getMaterialId)
                            .collect(Collectors.toSet());

                    // 필요한 재료와 주문 데이터만 필터링
                    return controlRedisQueryService.getRedisData()
                            .map(container -> filterMaterialsAndOrders(container, normalMaterialIds, currProc));
                });
    }

    // 코일 품종끼리 묶어서 카운팅
    private Map<String, Long> countCoilTypes(List<MaterialDTO.View> materials) {
        return materials.stream()
                .collect(Collectors.groupingBy(
                        MaterialDTO.View::getCoilTypeCode,
                        Collectors.counting()
                ));
    }

    // 고객사별 카운팅
    private Map<String, Long> countCustomers(RedisDataContainer container) {
        Map<Long, String> orderCustomerMap = container.getOrders().stream()
                .collect(Collectors.toMap(OrderDTO.View::getId, OrderDTO.View::getCustomer));

        return container.getMaterials().stream()
                .map(material -> orderCustomerMap.get(material.getOrderId()))
                .collect(Collectors.groupingBy(
                        customer -> customer,
                        Collectors.counting()
                ));
    }

    // 필터링 -> 레디스에서 해당 공정과 정상재인 재료 및 주문 필터링
    private RedisDataContainer filterMaterialsAndOrders(RedisDataContainer container, Set<Long> normalMaterialIds, String currProc) {
        List<MaterialDTO.View> filteredMaterials = container.getMaterials().stream()
                .filter(material -> currProc.equals(material.getCurrProc()) && normalMaterialIds.contains(material.getId()))
                .collect(Collectors.toList());

        Set<Long> orderIds = filteredMaterials.stream()
                .map(MaterialDTO.View::getOrderId)
                .collect(Collectors.toSet());

        List<OrderDTO.View> filteredOrders = container.getOrders().stream()
                .filter(order -> orderIds.contains(order.getId()))
                .collect(Collectors.toList());

        return RedisDataContainer.builder()
                .materials(filteredMaterials)
                .orders(filteredOrders)
                .build();
    }
}
