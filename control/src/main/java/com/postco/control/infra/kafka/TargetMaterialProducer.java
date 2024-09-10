package com.postco.control.infra.kafka;

import com.postco.core.dto.TargetMaterialDTO;
import com.postco.core.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TargetMaterialProducer {
    private static final String TOPIC = "control-targetMaterial-data";
    private final KafkaProducer genericProducer;

    public void sendMaterials(TargetMaterialDTO.View materials) {
        genericProducer.sendData(TOPIC, materials);
    }
}
