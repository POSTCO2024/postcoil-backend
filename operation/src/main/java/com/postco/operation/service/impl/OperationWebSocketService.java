package com.postco.operation.service.impl;

import com.postco.core.dto.CoilSupplyDTO;
import com.postco.core.dto.MaterialDTO;
import com.postco.operation.domain.entity.WorkInstruction;
import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.presentation.dto.WorkInstructionItemDTO;
import com.postco.operation.presentation.dto.websocket.ClientDTO;
import com.postco.operation.presentation.dto.websocket.ClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ClientMapper clientMapper;
    private final List<ClientDTO> buffer = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        // 5초마다 버퍼를 전송
        scheduler.scheduleAtFixedRate(this::flushBuffer, 0, 5, TimeUnit.SECONDS);
    }

    // 실시간 업데이트를 즉시 WebSocket으로 전송
    public <T> void sendRealTimeUpdate(T dto) {
        try {
            // 모든 메시지를 동일한 목적지 "/topic/coil"로 전송
            String destination = "/topic/coil";
            Object message = prepareMessage(dto);
            messagingTemplate.convertAndSend(destination, message);
            log.debug("실시간 업데이트 전송: {}, 대상: {}", message, destination);
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 중 오류 발생", e);
        }
    }

    // 메시지를 각 DTO에 맞게 변환
    private <T> Object prepareMessage(T dto) {
        if (dto instanceof WorkInstruction) {
            return clientMapper.mapToClientDTO((WorkInstruction) dto);
        } else if (dto instanceof CoilSupplyDTO) {
            return clientMapper.mapToCoilSupplyClientDTO((CoilSupplyDTO) dto);
        } else if (dto instanceof MaterialDTO.Message) {
            return clientMapper.mapToMaterialClientDTO((MaterialDTO.Message) dto);
        } else if (dto instanceof WorkInstructionItemDTO.View) {
            return clientMapper.mapToWorkInstructionItemClientDTO((WorkInstructionItemDTO.View) dto);
        }
        return dto; // 기본적으로는 변환 없이 DTO를 전송
    }

    // 작업 완료 시 데이터를 버퍼에 저장
    public void addToCompleteBuffer(WorkInstructionItem item) {
        ClientDTO dto = clientMapper.mapToClientDTO(item.getWorkInstruction());
        synchronized (buffer) {
            buffer.add(dto);
        }
        log.info("작업 완료 데이터 버퍼에 추가: {}", dto);
    }

    // 버퍼에 저장된 데이터를 일정 시간마다 전송
    private void flushBuffer() {
        List<ClientDTO> toSend;
        synchronized (buffer) {
            toSend = new ArrayList<>(buffer);
            buffer.clear();
        }
        if (!toSend.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/coil", toSend);
            log.info("버퍼에 저장된 데이터 전송: {}", toSend);
        }
    }
}