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
    private final OrderRepository orderRepository;
    private final WorkItemRepository workItemRepository;
    private final MaterialsProducer materialsProducer;
    private final OrderProducer orderProducer;

    @Override
    public void updateMaterialProgress(Long materialId, MaterialProgress newProgress) {
        Materials material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));
        material.updateProgress(newProgress);
        materialRepository.save(material);
    }

    @Override
    public void startWork(Long workItemId) {
        WorkInstructionItem item = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new EntityNotFoundException("Work instruction item not found"));
        item.startWork();
        workItemRepository.save(item);
    }

    @Override
    public void finishWork(Long workItemId) {
        WorkInstructionItem item = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new EntityNotFoundException("Work instruction item not found"));

        LocalDateTime expectedEndTime = item.getStartTime().plusSeconds(item.getExpectedItemDuration());
        if(LocalDateTime.now().isAfter(expectedEndTime)) {
            item.finishWork();
            updateMaterialProgress(item.getMaterial().getId(), MaterialProgress.H);
            workItemRepository.save(item);
        }
    }

    @Override
    public void requestTransfer(Long materialId) {
        Materials material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        if(material.getProgress() == MaterialProgress.H) {
            updateMaterialProgress(materialId, MaterialProgress.J);
        }
    }

    @Override
    public boolean checkTransferCompletion(Long materialId, Duration transferDuration) {
        Materials material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        if(isTransferCompleted()) {
            material.updateProgress(MaterialProgress.D);
            materialRepository.save(material);
            return true;
        }
        return false;
    }


    private boolean isTransferCompleted() {
        LocalDateTime startTime = LocalDateTime.now();
        // 50초 뒤로 설정
        Duration duration = Duration.ofSeconds(50);

        return LocalDateTime.now().isAfter(startTime.plus(duration));
    }
}
