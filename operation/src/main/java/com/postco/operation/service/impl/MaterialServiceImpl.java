package com.postco.operation.service.impl;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.utils.mapper.MapperUtils;
import com.postco.operation.domain.MaterialProgress;
import com.postco.operation.domain.Materials;
import com.postco.operation.domain.WorkInstructionItem;
import com.postco.operation.domain.repository.MaterialRepository;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.infra.kafka.MaterialsProducer;
import com.postco.operation.service.MaterialService;
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
public class MaterialServiceImpl implements MaterialService {
    private final MaterialRepository materialRepository;
    private final WorkItemRepository workItemRepository;
    private final MaterialsProducer materialsProducer;

    public void sendAllMaterials() {
        List<Materials> materialsList = materialRepository.findAll();
        // 특정 규칙 매핑 적용
        PropertyMap<Materials, MaterialDTO.View> map = new PropertyMap<>() {
            @Override
            protected void configure() {
                map(source.getOrder().getNo(), destination.getOrderNo());
            }
        };


        // 엔티티 리스트 -> DTO 변환
        List<MaterialDTO.View> viewDto = MapperUtils.mapListWithProperty(materialsList, MaterialDTO.View.class, map);

        // Kakfa 전송
        viewDto.forEach(materialsProducer::sendMaterials);
    }

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
