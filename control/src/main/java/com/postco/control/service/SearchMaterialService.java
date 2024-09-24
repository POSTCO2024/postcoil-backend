package com.postco.control.service;


import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.TargetViewDTO;
import com.postco.control.service.impl.redis.ControlRedisQueryService;
import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchMaterialService {
    private final TargetMaterialQueryService targetMaterialQueryService;
    private final TargetMaterialRepository targetMaterialRepository;
    private final ControlRedisQueryService controlRedisQueryService;
    private final ModelMapper modelMapper;

    /**
     * 특정 공정(currProc)과 검색 조건에 맞는 정상 작업 대상재를 조회하는 메서드
     * @param currProc 현재 공정
     * @param searchCriteria 검색 조건 (ex: coil_number, order_no 등)
     * @param searchValue 검색 값 (ex: 특정 코일 번호, 주문 번호 등)
     * @return Mono<List<TargetViewDTO>> 검색 조건에 맞는 TargetViewDTO 리스트
     * @param isError 에러 여부
     */

    public Mono<List<TargetViewDTO>> searchMaterialsByCurrProc(String currProc, String searchCriteria, String searchValue, String isError) {
        // 정상재 추출
        List<TargetMaterial> materials = targetMaterialRepository.findByIsError(isError);

        // Redis Mapping
        return getTargetViewDTOs(() -> mapToTargetMaterialDTO(materials))
                .map(targetViews -> targetViews.stream()
                        // 검색 조건 필터링
                        .filter(view -> currProc.equals(view.getMaterial().getCurrProc()))         // 공정
                        .filter(view -> applySearchCriteria(view, searchCriteria, searchValue))    // 검색
                        .collect(Collectors.toList()));
    }


    /**
     * 검색 결과가 있는지 확인
     * @param view
     * @param searchCriteria
     * @param searchValue
     * @return 키워드 존재 여부(True/False)
     */
    private boolean applySearchCriteria(TargetViewDTO view, String searchCriteria, String searchValue) {
        System.out.println("searchCriteria: " + searchCriteria + " searchValue: " + searchValue);

        if (searchCriteria.isEmpty() || searchValue.isEmpty()) {
            log.info("검색 조건 또는 값이 존재하지 않습니다. ");
            return true; // 검색 조건이 없으면 모든 데이터를 반환
        }
        Function<TargetViewDTO, String> searchFunction = searchCriteriaMap.get(searchCriteria);

        return searchFunction.apply(view).equals(searchValue);
    }

    private final Map<String, Function<TargetViewDTO, String>> searchCriteriaMap = Map.of(
            "material_id", view -> view.getMaterial().getNo(),
            "coil_type_code", view -> view.getMaterial().getCoilTypeCode(),
            "order_no", view -> view.getOrder().getNo(),
            "customer_name", view -> view.getOrder().getCustomer()
    );


    // Mapping
    private List<TargetMaterialDTO.View> mapToTargetMaterialDTO(List<TargetMaterial> materials) {
        return materials.stream()
                .map(material -> modelMapper.map(material, TargetMaterialDTO.View.class))
                .collect(Collectors.toList());
    }

    private Mono<List<TargetViewDTO>> getTargetViewDTOs(Supplier<List<TargetMaterialDTO.View>> targetMaterialsSupplier) {
        return controlRedisQueryService.getRedisData()
                .map(redisDataContainer -> {
                    Map<Long, MaterialDTO.View> materialMap = redisDataContainer.getMaterials().stream()
                            .collect(Collectors.toMap(MaterialDTO.View::getId, Function.identity()));
                    Map<Long, OrderDTO.View> orderMap = redisDataContainer.getOrders().stream()
                            .collect(Collectors.toMap(OrderDTO.View::getId, Function.identity()));

                    return targetMaterialsSupplier.get().stream()
                            .map(targetMaterial -> {
                                MaterialDTO.View material = materialMap.get(targetMaterial.getMaterialId());
                                OrderDTO.View order = material != null ? orderMap.get(material.getOrderId()) : null;

                                return TargetViewDTO.builder()
                                        .material(material)
                                        .order(order)
                                        .targetId(targetMaterial.getId())
                                        .processPlan(targetMaterial.getProcessPlan())
                                        .rollUnitName(targetMaterial.getRollUnitName())
                                        .isError(targetMaterial.getIsError())
                                        .errorType(targetMaterial.getErrorType())
                                        .isErrorPassed(targetMaterial.getIsErrorPassed())
                                        .build();
                            })
                            .collect(Collectors.toList());
                });
    }

}
