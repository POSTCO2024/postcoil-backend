package com.postco.core.entity;

import org.modelmapper.ModelMapper;

public interface Entity {
    default <T> T convert(Class<T> destinationType) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(this, destinationType);
    }
}
