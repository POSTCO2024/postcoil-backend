package com.postco.operation.service.impl;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.utils.mapper.MapperUtils;
import com.postco.operation.domain.entity.MaterialProgress;
import com.postco.operation.domain.entity.Materials;
import com.postco.operation.domain.entity.Order;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.repository.MaterialRepository;
import com.postco.operation.domain.repository.OrderRepository;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.infra.kafka.MaterialsProducer;
import com.postco.operation.infra.kafka.OrderProducer;
import com.postco.operation.service.MaterialUpdateService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MaterialUpdateServiceImpl implements MaterialUpdateService {
    private final MaterialRepository materialRepository;
    private final MaterialsProducer materialsProducer;
    private final OrderProducer orderProducer;

    @Override
    public void updateMaterialProgress(Long materialId, MaterialProgress newProgress) {
        Materials material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));
        material.updateProgress(newProgress);
        materialRepository.save(material);
    }
}