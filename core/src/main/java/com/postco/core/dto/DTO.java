package com.postco.core.dto;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public interface DTO {
    default <T> T convert(Class<T> destinationType) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(this, destinationType);
    }
}