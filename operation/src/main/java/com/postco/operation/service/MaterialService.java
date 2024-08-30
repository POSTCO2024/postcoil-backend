package com.postco.operation.service;

import com.postco.core.utils.mapper.MapperUtils;
import com.postco.operation.domain.Materials;
import com.postco.operation.domain.repository.MaterialsRepository;
import com.postco.operation.infra.kafka.MaterialsProducer;
import com.postco.operation.presentation.dto.MaterialsDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {
    private final MaterialsRepository materialsRepository;
    private final MaterialsProducer materialsProducer;

    public void sendAllMaterials() {
        List<Materials> materialsList = materialsRepository.findAll();
        // 특정 규칙 매핑 적용
        PropertyMap<Materials, MaterialsDTO.View> map = new PropertyMap<>() {
            @Override
            protected void configure() {
                map(source.getOrder().getNo(), destination.getOrderNo());
            }
        };

        // 엔티티 리스트 -> DTO 변환
        List<MaterialsDTO.View> viewDto = MapperUtils.mapListWithProperty(materialsList, MaterialsDTO.View.class, map);

        // Kakfa 전송
        viewDto.forEach(materialsProducer::sendMaterials);
    }
}
