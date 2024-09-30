package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.Materials;
import com.postco.operation.domain.repository.MaterialRepository;
import com.postco.operation.service.WebSocketService;
import com.postco.websocket.service.CoilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
    private final MaterialRepository materialRepository;
    private final CoilService coilService;


    @Override
    public List<Materials> findMaterials() {
        coilService.directMessageToClient("undecided", materialRepository.findAll().toString());
        return null;
    }
}
