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
import org.hibernate.dialect.SybaseAnywhereDialect;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchMaterialService {
    private final TargetMaterialRepository targetMaterialRepository;
    private final ControlRedisQueryService controlRedisQueryService;
    private final ModelMapper modelMapper;

    /**
     * 특정 공정(currProc)과 검색 조건에 맞는 정상 작업 대상재를 조회하는 메서드
     * @param currProc 현재 공정
     * @param searchCriteria 검색 조건 (ex: coil_number, order_no 등)
     * @param searchValue 검색 값 (ex: 특정 코일 번호, 주문 번호 등)
     * @param minValue 범위 최소 값
     * @param maxValue 범위 최대 값
     * @param isError 에러 여부
     * @return Mono<List<TargetViewDTO>> 검색 조건에 맞는 TargetViewDTO 리스트
     */

    public Mono<List<TargetViewDTO>> searchMaterialsByCurrProc(String currProc, String searchCriteria, String searchValue, String minValue, String maxValue, String isError) {
        // 정상재/에러재 조회
        List<TargetMaterial> materials = targetMaterialRepository.findByIsError(isError);

        // Redis Mapping
        return getTargetViewDTOs(() -> mapToTargetMaterialDTO(materials))
                .map(targetViews -> {
                    // log.debug("필터링 전 대상재 리스트 크기: {}", targetViews.size());

                    // 공정 및 검색 조건 필터링 적용
                    List<TargetViewDTO> filteredViews = targetViews.stream()
                            .filter(view -> currProc.equals(view.getMaterial().getCurrProc()))  // 공정 필터링
                            .filter(view -> applySearchCriteria(view, searchCriteria, searchValue, minValue, maxValue)) // 검색 필터링
                            .collect(Collectors.toList());

                    // log.debug("필터링 후 대상재 리스트 크기: {}", filteredViews.size());
                    return filteredViews;
                });
    }


    /**
     * 검색 결과가 있는지 확인
     * @param view
     * @param searchCriteria
     * @param searchValue
     * @param minValue
     * @param maxValue
     * @return 키워드 존재 여부(True/False)
     */
    private boolean applySearchCriteria(TargetViewDTO view, String searchCriteria, String searchValue, String minValue, String maxValue) {
        boolean matches = true;

        // 키워드 검색
        if (searchCriteria != null && !searchCriteria.isEmpty() && searchValue != null && !searchValue.isEmpty()) {
            Function<TargetViewDTO, Object> searchFunction = searchCriteriaMap.get(searchCriteria);
            if (searchFunction != null) {
                // searchValue 타입 확인(string)
                if (searchFunction.apply(view) instanceof String) {
                    matches = searchFunction.apply(view).equals(searchValue);
                }
            }
        }

        // 범위 검색
        if (minValue != null && maxValue != null && !minValue.isEmpty() && !maxValue.isEmpty()) {
            try {
                double min = Double.parseDouble(minValue);
                double max = Double.parseDouble(maxValue);

                Function<TargetViewDTO, Object> searchFunction = searchCriteriaMap.get(searchCriteria);
                // 타입 확인 (number)
                if (searchFunction != null && searchFunction.apply(view) instanceof Number) {
                    double value = ((Number) searchFunction.apply(view)).doubleValue();  // 숫자형 값 비교
                    return value >= min && value <= max;  // 범위 내에 있는지 확인
                }
            } catch (NumberFormatException e) {
                log.error("[TypeError] 숫자형 타입이 아니므로 검색이 불가능합니다. ", e);
                return false;
            }
        }


        return matches;
    }


    // Mapping
    private List<TargetMaterialDTO.View> mapToTargetMaterialDTO(List<TargetMaterial> materials) {
        return materials.stream()
                .map(material -> modelMapper.map(material, TargetMaterialDTO.View.class)) // TargetMaterial -> TargetMaterialDTO.View로 매핑
                .collect(Collectors.toList());
    }

    private final Map<String, Function<TargetViewDTO, Object>> searchCriteriaMap = Map.of(
            "material_id", view -> view.getMaterial().getNo(),
            "coil_type_code", view -> view.getMaterial().getCoilTypeCode(),
            "order_no", view -> view.getOrder().getNo(),
            "customer_name", view -> view.getOrder().getCustomer(),
            "width", view -> view.getMaterial().getWidth(),
            "thickness", view -> view.getMaterial().getThickness()
    );

    // Redis 및 DB에서 데이터를 매핑
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
