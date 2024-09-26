package com.postco.operation.service.impl;

import com.postco.core.dto.CoilSupplyDTO;
import com.postco.core.dto.MaterialDTO;
import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.presentation.dto.WorkInstructionDTO;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import com.postco.operation.presentation.dto.websocket.MessageDTO;
import com.postco.operation.presentation.dto.websocket.MessageMapper;
import com.postco.operation.presentation.dto.websocket.WebSocketMessageDTO;
import com.postco.operation.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;
    private final Map<Class<?>, Function<Object, MessageDTO>> dtoMappers = new HashMap<>();

    @PostConstruct
    public void init() {
        initDtoMappers();
    }

    private void initDtoMappers() {
        dtoMappers.put(WorkInstruction.class, dto -> messageMapper.mapToClientDTO((WorkInstruction) dto));
        dtoMappers.put(CoilSupplyDTO.Message.class, dto -> MessageDTO.builder().coilSupply(List.of(messageMapper.mapToCoilSupplyClientDTO((CoilSupplyDTO.Message) dto))).build());
        dtoMappers.put(MaterialDTO.Message.class, dto -> MessageDTO.builder().materials(List.of(messageMapper.mapToMaterialClientDTO((MaterialDTO.Message) dto))).build());
        dtoMappers.put(WorkInstructionItemDTO.Message.class, dto -> MessageDTO.builder().workItem(messageMapper.mapToWorkInstructionItemClientDTO((WorkInstructionItemDTO.Message) dto)).build());
        dtoMappers.put(WorkInstructionDTO.Message.class, dto -> MessageDTO.builder().workInstructions(List.of((WorkInstructionDTO.Message) dto)).build());
    }

    public <T> void sendMessage(T dto, WebSocketMessageType type) {
        log.debug("WebSocket 메시지 전송 시도 중입니다. DTO 유형: {}, WebSocket 메시지 유형: {}", dto.getClass().getSimpleName(), type);

        try {
            Optional.ofNullable(dtoMappers.get(dto.getClass()))
                    .map(mapper -> mapper.apply(dto))
                    .map(messageDto -> new WebSocketMessageDTO(type, messageDto))
                    .ifPresentOrElse(
                            message -> {
                                messagingTemplate.convertAndSend("/topic/coilData", message);
                                log.info("WebSocket 메시지 전송 성공. 유형: {}, 데이터: {}", type, message.getData());
                            },
                            () -> log.warn("WebSocket 메시지 생성 실패. DTO 유형: {}", dto.getClass().getSimpleName())
                    );
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 중 오류가 발생했습니다.", e);
        }
    }
}