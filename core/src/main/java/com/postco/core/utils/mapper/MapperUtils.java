package com.postco.core.utils.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 엔티티(Entity)와 DTO(Data Transfer Object) 간의 변환을 처리 ModelMapper를 클래스
 * 리스트나 페이지 형태의 데이터를 매핑할 때 유용하게 사용할 수 있습니다.
 *
 * 주요 기능:
 * - 단일 객체를 다른 타입의 객체로 변환
 * - 리스트 형태의 객체들을 다른 타입의 리스트로 변환
 * - 페이지 형태의 객체들을 다른 타입의 페이지로 변환
 * - 특정 매핑 규칙을 적용하여 매핑 작업 수행
 *
 *
 * ModelMapper의 인스턴스는 클래스 로딩 시 한 번만 생성되며, 공통 매핑 전략으로 STRICT 모드를 사용합니다.
 * 이를 통해 예상치 못한 필드 매핑을 방지하고, 일관된 매핑 전략을 적용할 수 있습니다.
 *
 */
public class MapperUtils {

    private static ModelMapper createModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    public static <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        ModelMapper modelMapper = createModelMapper();
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    public static <S, T> Page<T> mapPage(Page<S> source, Class<T> targetClass) {
        ModelMapper modelMapper = createModelMapper();
        return source.map(element -> modelMapper.map(element, targetClass));
    }

    public static <S, T> List<T> mapListWithProperty(List<S> source, Class<T> targetClass, PropertyMap<S, T> propertyMap) {
        ModelMapper modelMapper = createModelMapper();
        modelMapper.addMappings(propertyMap);
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }
}
