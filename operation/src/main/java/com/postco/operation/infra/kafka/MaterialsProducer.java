package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.core.exception.KafkaSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.postco.core.dto.MaterialDTO;


@Service
@RequiredArgsConstructor
public class MaterialsProducer {
    private static final String TOPIC = "operation-material-data";
    private final GenericProducer<MaterialDTO.View> genericProducer;

    public void sendMaterials(MaterialDTO.View materials) {
        genericProducer.sendData(TOPIC, materials);
    }
}