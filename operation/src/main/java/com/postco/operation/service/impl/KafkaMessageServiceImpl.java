package com.postco.operation.service.impl;

import com.postco.core.dto.MaterialDTO;
import com.postco.operation.infra.kafka.MaterialsProducer;
import com.postco.operation.service.KafkaMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaMessageServiceImpl implements KafkaMessageService {
    private final MaterialsProducer materialsProducer;
    @Override
    public void sendMaterials(MaterialDTO.View materials) {
        materialsProducer.sendMaterials(materials);
    }
}
