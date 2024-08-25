package com.postco.core.utils.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class MapperUtils {
    public static <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return source
                .stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    public static <S, T> Page<T> mapPage(Page<S> source, Class<T> targetClass) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return source.map(element -> modelMapper.map(element, targetClass));
    }

    public static <S, T> List<T> mapListWithProperty(List<S> source, Class<T> targetClass, PropertyMap<S, T> map) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(map);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return source
                .stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }
}
