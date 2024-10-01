package com.postco.operation.presentation.dto;

import com.postco.core.dto.ScheduleMaterialDTO;
import com.postco.core.dto.ScheduleResultDTO;
import com.postco.operation.domain.entity.Materials;
import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.entity.WorkStatus;
import com.postco.operation.domain.repository.MaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
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

        // ScheduleMaterialDTO -> WorkInstructionItemDTO.Create
        modelMapper.addMappings(new PropertyMap<ScheduleMaterialDTO, WorkInstructionItemDTO.Create>() {
            @Override
            protected void configure() {
                map().setMaterialId(source.getMaterialId());
                map().setTargetId(source.getTargetId());
                map().setInitialThickness(source.getThickness());
                map().setInitialGoalWidth(source.getGoalWidth());
                map().setWorkItemStatus(safeValueOf(source.getWorkStatus()));
                map().setIsRejected(source.getIsRejected());
                map().setExpectedItemDuration(source.getExpectedDuration());
            }
        });

        modelMapper.addMappings(new PropertyMap<WorkInstruction, WorkInstructionDTO.Message>() {
            @Override
            protected void configure() {
                map(source.getId(), destination.getWorkInstructionId());
                map(source.getWorkStatus(), destination.getSchStatus());
            }
        });

        modelMapper.addMappings(new PropertyMap<WorkInstruction, WorkInstructionDTO.View>() {
            @Override
            protected void configure() {
                map(source.getWorkStatus(), destination.getSchStatus());
            }
        });

        // WorkInstructionItem -> WorkInstructionItemDTO.View 매핑 추가
        modelMapper.addMappings(new PropertyMap<WorkInstructionItem, WorkInstructionItemDTO.View>() {
            @Override
            protected void configure() {
                map(source.getMaterial().getId(), destination.getMaterialId()); // material의 id를 materialId로 매핑
                map(source.getMaterial().getNo(), destination.getMaterialNo());
                map(source.getMaterial().getPreProc(), destination.getPreProc());
                map(source.getMaterial().getNextProc(), destination.getNextProc());
                map(source.getMaterial().getTemperature(), destination.getTemperature());
                map(source.getMaterial().getWeight(), destination.getWeight());
                map(source.getMaterial().getLength(), destination.getLength());

            }
        });
    }

    // ScheduleResultDTO.View -> WorkInstructionDTO.Create 매핑
    public static WorkInstructionDTO.Create mapToWorkInstructionDTO(ScheduleResultDTO.View scheduleResult, String workNo) {
        WorkInstructionDTO.Create dto = modelMapper.map(scheduleResult, WorkInstructionDTO.Create.class);
        dto.setWorkNo(workNo);
        log.info("스케줄의 아이템 : {]", scheduleResult.getMaterials());
        dto.setItems(scheduleResult.getMaterials() != null
                ? mapToWorkInstructionItemDTOs(scheduleResult.getMaterials())
                : null);
        return dto;
    }

    // SCHMaterialDTO 리스트 -> WorkInstructionItemDTO.Create 리스트 매핑
    private static List<WorkInstructionItemDTO.Create> mapToWorkInstructionItemDTOs(List<ScheduleMaterialDTO> materials) {
        return materials.stream()
                .map(material -> {
                    log.info("SCHMaterialDTO : {}", material);
                    return modelMapper.map(material, WorkInstructionItemDTO.Create.class);

                })
                .collect(Collectors.toList());
    }

    // WorkInstructionDTO.Create -> WorkInstruction 엔티티 매핑
    public static WorkInstruction mapToEntity(WorkInstructionDTO.Create dto, MaterialRepository materialRepository) {
        WorkInstruction workInstruction = modelMapper.map(dto, WorkInstruction.class);
        workInstruction.setItems(new ArrayList<>()); // 명시적으로 빈 리스트 초기화

        if (dto.getItems() != null) {
            Set<Long> addedMaterialIds = new HashSet<>();
            dto.getItems().forEach(itemDto -> {
                if (!addedMaterialIds.contains(itemDto.getMaterialId())) {
                    WorkInstructionItem item = mapToItemEntity(itemDto, materialRepository);
                    item.setWorkInstruction(workInstruction);
                    workInstruction.getItems().add(item);
                    addedMaterialIds.add(itemDto.getMaterialId());
                }
            });
        }

        return workInstruction;
    }


    // WorkInstructionItemDTO.Create -> WorkInstructionItem 엔티티 매핑
    private static WorkInstructionItem mapToItemEntity(WorkInstructionItemDTO.Create itemDto, MaterialRepository materialRepository) {
        WorkInstructionItem item = modelMapper.map(itemDto, WorkInstructionItem.class);

        // materialId를 기반으로 Materials 엔티티 찾기
        Materials material = materialRepository.findById(itemDto.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("Material not found for id: " + itemDto.getMaterialId()));

        item.setMaterial(material);
        return item;
    }

    // WorkInstruction 엔티티 -> WorkInstructionDTO.Message 매핑
    public static WorkInstructionDTO.View mapToDto(WorkInstruction entity) {
        WorkInstructionDTO.View dto = modelMapper.map(entity, WorkInstructionDTO.View.class);
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                    .map(WorkInstructionMapper::mapToItemDto)
                    .collect(Collectors.toList()));
        }
        log.info("mapToDto : {}", dto);
        return dto;
    }

    public static WorkInstructionDTO.Message mapToMessageDto(WorkInstruction entity) {
        WorkInstructionDTO.Message dto = modelMapper.map(entity, WorkInstructionDTO.Message.class);
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                    .map(WorkInstructionMapper::mapToItemMessageDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }


    // WorkInstructionItem 엔티티 -> WorkInstructionItemDTO.View 매핑
    public static WorkInstructionItemDTO.View mapToItemDto(WorkInstructionItem item) {
        log.info("mapToDto : {}", modelMapper.map(item, WorkInstructionItemDTO.View.class));
        return modelMapper.map(item, WorkInstructionItemDTO.View.class);
    }

    public static WorkInstructionItemDTO.Message mapToItemMessageDto(WorkInstructionItem item) {
        return modelMapper.map(item, WorkInstructionItemDTO.Message.class);
    }


    private static WorkStatus safeValueOf(String status) {
        try {
            return status != null ? WorkStatus.valueOf(status) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}