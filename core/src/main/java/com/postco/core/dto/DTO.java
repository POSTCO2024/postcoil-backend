package com.postco.core.dto;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Map;

public interface DTO {
    default <T> T convert(Class<T> destinationType) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(this, destinationType);
    }

    static <T extends DTO> T fromMap(Map<Object, Object> map, Class<T> dtoClass) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(map, dtoClass);
    }
}