package com.postco.core.utils.mapper;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.TargetMaterialDTO;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetMaterialMapper {
    public static final ModelMapper modelMapper = new ModelMapper();
    static {
        Converter<LocalDateTime, String> localDateTimeToStringConverter = new Converter<LocalDateTime, String>() {
            @Override
            public String convert(MappingContext<LocalDateTime, String> context) {
                return context.getSource() != null
                        ? context.getSource().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : null;
            }
        };

        // MaterialDTO.View -> TargetMaterialDTO.View 매핑
        modelMapper.addMappings(new PropertyMap<MaterialDTO.View, TargetMaterialDTO.View>() {
            @Override
            protected void configure() {
                map().setMaterialId(source.getId());
                map().setMaterialNo(source.getNo());
                map().setGoalWidth(source.getGoalWidth());
                map().setGoalThickness(source.getGoalThickness());
                map().setWeight(source.getWeight());
                map().setCoilTypeCode(source.getCoilTypeCode());

                // processPlan 매핑 로직
                using((Converter<MaterialDTO.View, String>) context -> {
                    MaterialDTO.View source = context.getSource();
                    if (source == null) {
                        return ""; // 기본값 처리
                    }

                    return Stream.of(source.getPassProc(), source.getRemProc())
                            .filter(s -> s != null && !s.isEmpty() && !s.equals("null"))
                            .collect(Collectors.joining());
                }).map(source, destination.getProcessPlan());
            }
        });

        // OrderDTO.View -> TargetMaterialDTO.View 매핑
        modelMapper.addMappings(new PropertyMap<OrderDTO.View, TargetMaterialDTO.View>() {
            @Override
            protected void configure() {
                map().setOrderNo(source.getNo());
                map().setGoalLength(source.getLength());
                using(localDateTimeToStringConverter).map(source.getDueDate(), destination.getDueDate());
                map().setCustomerName(source.getCustomer());
                map().setRemarks(source.getRemarks());
            }
        });
    }

    public static TargetMaterialDTO.View mapToTargetMaterial(MaterialDTO.View material, OrderDTO.View order) {
        TargetMaterialDTO.View targetMaterialDTO = modelMapper.map(material, TargetMaterialDTO.View.class);

        // 주문 정보가 있으면 추가로 매핑
        if (order != null) {
            modelMapper.map(order, targetMaterialDTO);
        }

        return targetMaterialDTO;
    }
}
