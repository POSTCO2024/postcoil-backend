package com.postco.operation.infra.kafka;

import com.postco.core.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.postco.core.dto.MaterialDTO;


@Service
@RequiredArgsConstructor
public class MaterialsProducer {
    private static final String TOPIC = "operation-material-data";
    private final KafkaProducer genericProducer;

    public void sendMaterials(MaterialDTO.View materials) {
        String key = String.valueOf(materials.getId());
        genericProducer.sendData(TOPIC, key, materials);
    }
}