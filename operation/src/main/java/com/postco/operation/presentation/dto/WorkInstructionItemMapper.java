package com.postco.operation.presentation.dto;

import com.postco.operation.domain.entity.Materials;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.repository.MaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WorkInstructionItemMapper {
    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // WorkInstructionItem -> SimulationItemDTO 매핑
        modelMapper.addMappings(new PropertyMap<WorkInstructionItem, WorkInstructionItemDTO.SimulationItemDTO>() {
            @Override
            protected void configure() {
                map(source.getMaterial().getId(), destination.getMaterialId());
                map(source.getId(), destination.getWorkItemId());
                map(source.getSequence(), destination.getSequence());
                map(source.getExpectedItemDuration(), destination.getExpectedItemDuration());
                map(source.getInitialThickness(), destination.getInitialThickness());
                map(source.getInitialGoalWidth(), destination.getInitialGoalWidth());
                map(source.getInitialGoalThickness(), destination.getInitialGoalThickness() );
                map(source.getInitialWidth(), destination.getInitialWidth());
            }
        });
    }

    // WorkInstructionItem 엔티티 -> SimulationItemDTO 매핑
    public static WorkInstructionItemDTO.SimulationItemDTO mapToSimulationItemDTO(WorkInstructionItem item) {
        return modelMapper.map(item, WorkInstructionItemDTO.SimulationItemDTO.class);
    }

    // WorkInstructionItemDTO.Create -> WorkInstructionItem 엔티티 매핑
    public static WorkInstructionItem mapToEntity(WorkInstructionItemDTO.Create dto, MaterialRepository materialRepository) {
        WorkInstructionItem item = modelMapper.map(dto, WorkInstructionItem.class);

        // Material 엔티티 매핑
        Materials material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("Material not found for id: " + dto.getMaterialId()));
        item.setMaterial(material);

        return item;
    }

    // WorkInstructionItem 리스트 -> SimulationItemDTO 리스트 매핑
    public static List<WorkInstructionItemDTO.SimulationItemDTO> mapToSimulationItemDTOList(List<WorkInstructionItem> items) {
        return items.stream()
                .map(WorkInstructionItemMapper::mapToSimulationItemDTO)
                .collect(Collectors.toList());
    }
}
