package com.postco.operation.service.impl;

import com.postco.operation.infra.kafka.MaterialsProducer;
import com.postco.operation.presentation.dto.MaterialsDTO;
import com.postco.operation.service.KafkaMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaMessageServiceImpl implements KafkaMessageService {
    private final MaterialsProducer materialsProducer;
    @Override
    public void sendMaterials(MaterialsDTO.View materials) {
        materialsProducer.sendMaterials(materials);
    }
}
