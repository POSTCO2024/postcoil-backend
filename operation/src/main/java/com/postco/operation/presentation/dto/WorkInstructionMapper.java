package com.postco.operation.presentation.dto;

import com.postco.core.dto.SCHMaterialDTO;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.entity.WorkStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class WorkInstructionMapper {
    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // ScheduleResultDTO.View -> WorkInstructionDTO.Create
        modelMapper.addMappings(new PropertyMap<ScheduleResultDTO.View, WorkInstructionDTO.Create>() {
            @Override
            protected void configure() {
                skip(destination.getWorkNo());
                map(source.getId(), destination.getScheduleId());
                map().setStartTime(LocalDateTime.now());
                map().setWorkStatus(safeValueOf(source.getWorkStatus()));
                map().setTotalQuantity(source.getQuantity());
                map().setExpectedDuration(source.getScExpectedDuration());
            }
        });

        // SCHMaterialDTO -> WorkInstructionItemDTO.Create
        modelMapper.addMappings(new PropertyMap<SCHMaterialDTO, WorkInstructionItemDTO.Create>() {
            @Override
            protected void configure() {
                map().setMaterialId(source.getId());
                map().setWorkItemStatus(safeValueOf(source.getWorkStatus()));
                map().setIsRejected(source.getIsRejected());
                map().setExpectedItemDuration(source.getExpectedDuration());
            }
        });
    }

    // ScheduleResultDTO.View -> WorkInstructionDTO.Create 매핑
    public static WorkInstructionDTO.Create mapToWorkInstructionDTO(ScheduleResultDTO.View scheduleResult, String workNo) {
        WorkInstructionDTO.Create dto = modelMapper.map(scheduleResult, WorkInstructionDTO.Create.class);
        dto.setWorkNo(workNo);
        dto.setItems(scheduleResult.getMaterials() != null
                ? mapToWorkInstructionItemDTOs(scheduleResult.getMaterials())
                : null);
        return dto;
    }

    // SCHMaterialDTO 리스트 -> WorkInstructionItemDTO.Create 리스트 매핑
    private static List<WorkInstructionItemDTO.Create> mapToWorkInstructionItemDTOs(List<SCHMaterialDTO> materials) {
        return materials.stream()
                .map(material -> modelMapper.map(material, WorkInstructionItemDTO.Create.class))
                .collect(Collectors.toList());
    }

    // WorkInstructionDTO.Create -> WorkInstruction 엔티티 매핑
    public static WorkInstruction mapToEntity(WorkInstructionDTO.Create dto) {
        WorkInstruction workInstruction = modelMapper.map(dto, WorkInstruction.class);

        if (dto.getItems() != null) {
            List<WorkInstructionItem> items = dto.getItems().stream()
                    .map(WorkInstructionMapper::mapToItemEntity)
                    .collect(Collectors.toList());
            items.forEach(workInstruction::addItem);
        }

        return workInstruction;
    }

    // WorkInstructionItemDTO.Create -> WorkInstructionItem 엔티티 매핑
    private static WorkInstructionItem mapToItemEntity(WorkInstructionItemDTO.Create itemDto) {
        return modelMapper.map(itemDto, WorkInstructionItem.class);
    }

    // WorkInstruction 엔티티 -> WorkInstructionDTO.View 매핑
    public static WorkInstructionDTO.View mapToDTO(WorkInstruction entity) {
        return modelMapper.map(entity, WorkInstructionDTO.View.class);
    }

    private static WorkStatus safeValueOf(String status) {
        try {
            return status != null ? WorkStatus.valueOf(status) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}