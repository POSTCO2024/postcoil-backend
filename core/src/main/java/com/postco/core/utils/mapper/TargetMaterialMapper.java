package com.postco.core.utils.mapper;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.dto.TargetMaterialDTO;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetMaterialMapper {
    public static final ModelMapper modelMapper = new ModelMapper();

    static {
        Converter<LocalDateTime, String> localDateTimeToStringConverter = context -> context.getSource() != null
                ? context.getSource().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        // MaterialDTO.View -> TargetMaterialDTO.Create 매핑
        modelMapper.addMappings(new PropertyMap<MaterialDTO.View, TargetMaterialDTO.Create>() {
            @Override
            protected void configure() {
                map().setMaterialId(source.getMaterialId());  // materialId 명시적 매핑
                map().setMaterialNo(source.getMaterialNo());

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

        // OrderDTO.View -> TargetMaterialDTO.Create 매핑
        modelMapper.addMappings(new PropertyMap<OrderDTO.View, TargetMaterialDTO.Create>() {
            @Override
            protected void configure() {
                map().setOrderNo(source.getNo());
                map().setGoalWidth(source.getWidth());
                map().setGoalThickness(source.getThickness());
                map().setGoalLength(source.getLength());
                using(localDateTimeToStringConverter).map(source.getDueDate(), destination.getDueDate());
                map().setCustomerName(source.getCustomer());
                map().setRemarks(source.getRemarks());
            }
        });
    }

    // MaterialDTO와 OrderDTO를 TargetMaterialDTO.Create로 매핑하는 메서드
    public static TargetMaterialDTO.Create mapToTargetMaterialCreate(MaterialDTO.View material, OrderDTO.View order) {
        TargetMaterialDTO.Create targetMaterialDTO = modelMapper.map(material, TargetMaterialDTO.Create.class);

        // 주문 정보가 있으면 추가로 매핑
        if (order != null) {
            modelMapper.map(order, targetMaterialDTO);
        }

        return targetMaterialDTO;
    }
}