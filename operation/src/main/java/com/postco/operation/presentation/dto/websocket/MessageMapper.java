package com.postco.operation.presentation.dto.websocket;

import com.postco.core.dto.CoilSupplyDTO;
import com.postco.core.dto.MaterialDTO;
import com.postco.operation.domain.entity.CoilSupply;
import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.repository.CoilSupplyRepository;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import com.postco.operation.presentation.dto.WorkInstructionMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    private final ModelMapper modelMapper;
    private final CoilSupplyRepository coilSupplyRepository;

    // WorkInstruction -> ClientDTO 변환
    public MessageDTO mapToClientDTO(WorkInstruction workInstruction) {
        List<WorkInstructionItem> workItems = workInstruction.getItems();
        CoilSupply coilSupply = coilSupplyRepository.findByWorkInstructionId(workInstruction.getId())
                .orElseThrow(() -> new IllegalStateException("CoilSupply not found for WorkInstruction: " + workInstruction.getId()));

        return MessageDTO.builder()
                .coilSupply(List.of(mapToCoilSupplyDTO(coilSupply)))
                .workInstructions(List.of(WorkInstructionMapper.mapToMessageDto(workInstruction)))
                .workItem(mapToWorkInstructionItemDTOs(workItems))
                .coilTypeCount(calculateCoilTypeCount(workInstruction))
                .totalWorkTime(calculateTotalWorkTime(workItems))
                .build();
    }

    // CoilSupplyDTO 변환
    public CoilSupplyDTO.Message mapToCoilSupplyClientDTO(CoilSupplyDTO.Message coilSupplyDTO) {
        return modelMapper.map(coilSupplyDTO, CoilSupplyDTO.Message.class);
    }

    // MaterialDTO 변환
    public MaterialDTO.Message mapToMaterialClientDTO(MaterialDTO.Message materialMessage) {
        return modelMapper.map(materialMessage, MaterialDTO.Message.class);
    }

    // WorkInstructionItemDTO 변환
    public List<WorkInstructionItemDTO.Message> mapToWorkInstructionItemClientDTO(WorkInstructionItemDTO.Message workInstructionItemDTO) {
        return List.of(workInstructionItemDTO);
    }

    // 추가적인 DTO 변환 로직은 기존과 동일하게 처리
    private CoilSupplyDTO.Message   mapToCoilSupplyDTO(CoilSupply coilSupply) {
        return modelMapper.map(coilSupply, CoilSupplyDTO.Message.class);
    }

    private List<WorkInstructionItemDTO.Message> mapToWorkInstructionItemDTOs(List<WorkInstructionItem> workItems) {
        return workItems.stream()
                .map(WorkInstructionMapper::mapToItemMessageDto)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> calculateCoilTypeCount(WorkInstruction workInstruction) {
        return workInstruction.getItems().stream()
                .map(item -> item.getMaterial().getCoilTypeCode())
                .collect(Collectors.groupingBy(
                        coilType -> coilType,
                        Collectors.summingInt(coilType -> 1)
                ));
    }

    private String calculateTotalWorkTime(List<WorkInstructionItem> workItems) {
        long totalSeconds = workItems.stream()
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
                .mapToLong(item -> Duration.between(item.getStartTime(), item.getEndTime()).getSeconds())
                .sum();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}