package com.postco.operation.presentation.dto.websocket;

import com.postco.core.dto.CoilSupplyDTO;
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
public class ClientMapper {
    private final ModelMapper modelMapper;
    private final CoilSupplyRepository coilSupplyRepository;

//    public ClientDTO mapToClientDTO(WorkInstruction workInstruction) {
//        List<WorkInstructionItem> workItems = workInstruction.getItems();
//        CoilSupply coilSupply = coilSupplyRepository.findByWorkInstructionId(workInstruction.getId())
//                .orElseThrow(() -> new IllegalStateException("CoilSupply not found for WorkInstruction: " + workInstruction.getId()));
//
////        return ClientDTO.builder()
////                .coilSupply(mapToCoilSupplyDTO(coilSupply))
////                .workItem(mapToWorkInstructionItemDTOs(workItems))
////                .coilTypeCount(calculateCoilTypeCount(workItems))
////                .totalWorkTime(calculateTotalWorkTime(workItems))
////                .build();
//        return true;
//    }

    private CoilSupplyDTO mapToCoilSupplyDTO(CoilSupply coilSupply) {
        return modelMapper.map(coilSupply, CoilSupplyDTO.class);
    }

    private List<WorkInstructionItemDTO.View> mapToWorkInstructionItemDTOs(List<WorkInstructionItem> workItems) {
        return workItems.stream()
                .map(WorkInstructionMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> calculateCoilTypeCount(List<WorkInstructionItem> workItems) {
        return workItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getMaterial().getCoilTypeCode(),
                        Collectors.summingInt(e -> 1)
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
