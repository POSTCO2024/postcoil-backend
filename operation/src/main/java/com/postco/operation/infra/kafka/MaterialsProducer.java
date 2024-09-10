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
        genericProducer.sendData(TOPIC, materials);
    }
}